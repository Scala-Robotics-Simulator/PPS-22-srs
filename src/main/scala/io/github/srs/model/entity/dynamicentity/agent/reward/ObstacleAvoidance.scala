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

    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ObstacleAvoidanceState,
    ): (Double, ObstacleAvoidanceState) =
      val currentMin = distanceFromObstacle(current.environment, entity)
      val rExpl = explorationReward(entity, state) * 0 // TODO: remove if this works
      val rClear = clearanceReward(prev.environment, current.environment, entity)
      val rColl = if currentMin < CollisionTriggerDistance then -1000.0 else 0.0
      val prevAgent = getAgentFromId(entity, prev)
      val rMove = moveReward(entity, prevAgent, action, currentMin)

      logger.info(s"evaluation at tick: ${state.ticks}")
      logger.info(s"exploration reward: $rExpl")
      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      logger.info(s"movement penalty: $rMove")

      val reward = rExpl + rClear + rColl + rMove
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

    private def moveReward(agent: Agent, prevAgent: Agent, action: Action[?], currentMin: Double): Double =
      if agent.position == prevAgent.position then if currentMin < 0.1 then 0.5 else -1.0
      else
        action match
          case ma: MovementAction[?] if ma.leftSpeed == 0.0 || ma.rightSpeed == 0.0 =>
            if currentMin > 0.5 then -1.0 else 1.0
          case ma: MovementAction[?] if ma.leftSpeed > 0 && ma.rightSpeed > 0 => 2.0
          case _ => -1.0

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

    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      val agent =
        env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
      agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

  end ObstacleAvoidance

  final case class SimpleObstacleAvoidance() extends RewardModel[Agent]:
    private val logger = Logger(getClass.getName)

    // private def getAgentFromId(agent: Agent, state: BaseState): Agent =
    //   state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)
    //
    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      val agent =
        env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
      agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

    private def simpleForwardBonus(action: Action[?]): Double =
      action match
        case ma: MovementAction[?] =>
          val avgSpeed = (ma.leftSpeed + ma.rightSpeed) / 2.0
          val speedDiff = abs(ma.leftSpeed - ma.rightSpeed)
          // Reward forward motion, penalize turning
          if avgSpeed > 0 && speedDiff < 0.3 then
            logger.info("Going forward")
            0.5
          else
            logger.info("Spinning")
            -0.5
        case _ => 0.0

    override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Double =
      val currentMin = distanceFromObstacle(current.environment, entity)

      // Core rewards only
      val rColl = if currentMin < CollisionTriggerDistance then -100.0 else 0.0
      val rClear = (currentMin - distanceFromObstacle(prev.environment, entity)) * 10.0
      val rForward = simpleForwardBonus(action)
      logger.info(s"rColl: $rColl")
      logger.info(s"rClear: $rClear")
      logger.info(s"rForward: $rForward")

      val reward = rColl + rClear + rForward
      logger.info(s"rTot: $reward")
      reward
  end SimpleObstacleAvoidance

end ObstacleAvoidanceRewardModule
