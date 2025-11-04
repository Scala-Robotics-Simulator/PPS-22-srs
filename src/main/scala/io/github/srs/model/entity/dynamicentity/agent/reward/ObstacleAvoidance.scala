package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.{ abs, exp, min }

import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment
import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import utils.types.CircularBuffer
import io.github.srs.model.entity.dynamicentity.action.MovementAction
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.logger

object ObstacleAvoidanceRewardModule:

  final protected case class ObstacleAvoidanceState(
      var ticks: Int = 0,
      val positions: CircularBuffer[Point2D] = CircularBuffer(200),
      val actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      maxTicks: Int = 10_000,
  )

  object ObstacleAvoidanceRewardStateManager extends RewardStateManager[Agent, ObstacleAvoidanceState]:
    override def createState(): ObstacleAvoidanceState = ObstacleAvoidanceState()

  /**
   * Reward model focused on obstacle avoidance.
   */
  final case class ObstacleAvoidance() extends StatefulReward[Agent, ObstacleAvoidanceState]:
    private val logger = Logger(getClass.getName)

    override protected def stateManager: RewardStateManager[Agent, ObstacleAvoidanceState] =
      ObstacleAvoidanceRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ObstacleAvoidanceState,
    ): (Double, ObstacleAvoidanceState) =
      val currentMin = distanceFromObstacle(current.environment, entity)
      val rExpl = explorationReward(entity, state) * 5
      val rSurv = survivalReward(state.ticks, state.maxTicks)
      val rClear = clearanceReward(prev.environment, current.environment, entity)
      val rColl = if currentMin < CollisionTriggerDistance then -100.0 else 0.0
      val rMove = moveReward(action, state)

      logger.info(s"evaluation at tick: ${state.ticks}")
      logger.info(s"exploration reward: $rExpl")
      logger.info(s"survival reward: $rSurv")
      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      logger.info(s"movement penalty: $rMove")

      val reward = rExpl + rSurv + rClear + rColl + rMove
      logger.info(s"total reward: $reward")

      val newPos =
        if state.ticks % 100 == 0 then state.positions.add(entity.position) else state.positions

      (
        reward,
        state.copy(
          ticks = state.ticks + 1,
          positions = newPos,
          actionHistory = state.actionHistory.add(action),
          maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
        ),
      )
    end compute

    private def clearanceReward(prev: Environment, current: Environment, entity: Agent): Double =
      val prevMin = distanceFromObstacle(prev, entity)
      val currMin = distanceFromObstacle(current, entity)
      val delta = currMin - prevMin
      val rChange = delta * 10.0
      val rProximity = -exp(-5 * currMin)
      (rChange + rProximity) / 2

    private def explorationReward(entity: Agent, state: ObstacleAvoidanceState): Double =
      val displacement = state.positions.map(entity.position.distanceTo).minOption.getOrElse(0.0)
      displacement

    private def survivalReward(duration: Int, maxDuration: Int): Double =
      val t = duration.toDouble.max(1.0)
      val K = 0.1
      val norm = math.log(1.0 + maxDuration)
      val totalSoFar = K * math.log(1.0 + t) / norm
      val totalPrev = K * math.log(1.0 + (t - 1.0)) / norm
      val rSurvStep = totalSoFar - totalPrev
      rSurvStep

    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      val agent =
        env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
      agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

    private def moveReward(action: Action[?], state: ObstacleAvoidanceState): Double =
      // Don't add here - already added in evaluate()

      val movementActions = state.actionHistory.collect { case ma: MovementAction[?] => ma }

      val (actLeft, actRight) = action match
        case ma: MovementAction[?] => (ma.leftSpeed, ma.rightSpeed)
        case _ => (0.0, 0.0)

      // Detect spinning: opposite wheel directions with similar magnitudes
      val isSpinning = (actLeft * actRight < 0) && (abs(abs(actLeft) - abs(actRight)) < 0.3)

      // Strong penalty for spinning (will be multiplied by 20 in evaluate)
      val spinPenalty = if isSpinning then -2.0 else 0.0

      // Detect oscillation only if we have enough history
      val oscillationPenalty = if movementActions.sizeIs >= 10 then
        val avgLeft = movementActions.map(_.leftSpeed).sum / movementActions.size
        val avgRight = movementActions.map(_.rightSpeed).sum / movementActions.size

        val dl = abs(actLeft - avgLeft)
        val dr = abs(actRight - avgRight)
        val isOscillating = (dl + dr) > 0.5 // lowered threshold

        if isOscillating then -(dl + dr) * 0.2 else 0.0
      else 0.0

      // Reward forward movement
      val forwardBonus = if actLeft > 0 && actRight > 0 && abs(actLeft - actRight) < 0.2 then 0.5 else 0.0

      spinPenalty + oscillationPenalty + forwardBonus
    end moveReward

  end ObstacleAvoidance

  final case class SimpleObstacleAvoidance() extends RewardModel[Agent]:

    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)

    override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Double =
      val prevAgent = getAgentFromId(entity, prev)
      val movementReward = if prevAgent.position == entity.position then -1.0 else 0.1
      val distances = entity.senseAll[Id](current.environment).proximityReadings.map(_.value)
      val distanceFromObstacles = distances.sum
      val prevDistanceFromObstacles = prevAgent.senseAll[Id](prev.environment).proximityReadings.map(_.value).sum
      val clearanceReward = if distanceFromObstacles > prevDistanceFromObstacles then 1.0 else -1.0
      val collisionReward =
        if distances.foldLeft(1.0)((acc, v) => min(acc, v)) < CollisionTriggerDistance then -100 else 0
      logger.info(
        s"SimpleObstacleAvoidance - movementReward: $movementReward, clearanceReward: $clearanceReward, collisionReward: $collisionReward",
      )
      movementReward + clearanceReward + collisionReward

end ObstacleAvoidanceRewardModule
