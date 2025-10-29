package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.collection.mutable
import scala.math.{ abs, min }

import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import utils.types.CircularBuffer

object ExplorationReward:

  case class ExplorationState(
      var ticks: Int = 0,
      positions: CircularBuffer[Point2D] = CircularBuffer(200),
      actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      visitedCells: mutable.Set[(Int, Int)] = mutable.Set(),
      maxTicks: Int = 10_000,
  )

  private object ExplorationRewardStateManager extends RewardStateManager[Agent, ExplorationState]:
    override def createState(): ExplorationState = ExplorationState()

  final case class Exploration() extends StatefulReward[Agent, ExplorationState]:
    private val logger = Logger(getClass.getName)

    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
      ExplorationRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ExplorationState,
    ): (Double, ExplorationState) =

      val currentMin = distanceFromObstacle(current.environment, entity)

      val rClear = clearanceReward(currentMin)
      val rCoverage = coverageReward(entity, state)
      val rMove = moveReward(action, state)
      val rExpl = explorationReward(entity, state)
      val rColl = if currentMin < CollisionTriggerDistance then -1000.0 else 0.0

      val reward = 1.0 * rClear + 3.0 * rCoverage + 1.0 * rMove + 2.0 * rExpl + rColl

      logger.info(
        f"Tick [${state.ticks}] | Pos: (${entity.position.x}%.2f, ${entity.position.y}%.2f) | Reward: $reward%.3f",
      )
      logger.info(f"rClear, $rClear, rCoverage $rCoverage, rMove $rMove, rExpl $rExpl, rColl $rColl")

      state.positions.add(entity.position): Unit

      val cellSize = 1.0
      val pos = entity.position
      val cellX = (pos.x / cellSize).toInt
      val cellY = (pos.y / cellSize).toInt

      state.visitedCells += ((cellX, cellY))

      (
        reward,
        state.copy(
          ticks = state.ticks + 1,
          positions = state.positions,
          visitedCells = state.visitedCells,
          actionHistory = state.actionHistory.add(action),
          maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
        ),
      )
    end compute

  end Exploration

  private def clearanceReward(minDist: Double): Double =
    val safeDistance = 0.3
    val K = 5.0
    if minDist < safeDistance then -K * (safeDistance - minDist)
    else 0.0

  private def explorationReward(entity: Agent, state: ExplorationState): Double =
    if state.positions.sizeIs < 2 then 0.0
    else
      val last = state.positions.lastOption.getOrElse(entity.position)
      val dist = entity.position.distanceTo(last)

      val immobilityPenalty =
        if dist < 0.05 then -0.05 * math.min(state.ticks, 20) // limita a 20 tick
        else 0.0

      val smallMoveBonus = if dist >= 0.05 then dist else 0.01

      smallMoveBonus + immobilityPenalty

  // TODO
  private def coverageReward(entity: Agent, state: ExplorationState): Double =
    val pos = entity.position
    val cellSize = 1.0
    val cell = ((pos.x / cellSize).toInt, (pos.y / cellSize).toInt)
    val alreadyVisited = state.visitedCells.toSeq.contains(cell)
    if !alreadyVisited then 100.0 else 0.05

  // TODO
//  private def noveltyReward(entity: Agent, state: ExplorationState): Double =
//    val pos = entity.position
//    val distances = state.visitedCells.toSeq.map(_.distanceTo(pos))
//    val minDistance = distances.foldLeft(Double.MaxValue) { (acc, d) =>
//      if (d < acc) d else acc
//    }
//    if minDistance > 1.0 then 1.0 else 0.0

  // TODO
  private def moveReward(action: Action[?], state: ExplorationState): Double =
    val movementActions = state.actionHistory.collect { case ma: MovementAction[?] => ma }
    val (actLeft, actRight) = action match
      case ma: MovementAction[?] => (ma.leftSpeed, ma.rightSpeed)
      case _ => (0.0, 0.0)

    val lastActions = movementActions.takeRight(5)
    val spinPenalty =
      if lastActions.nonEmpty && lastActions.forall(a => a.leftSpeed * a.rightSpeed < 0) then -2.0 else 0.0

    val oscillationPenalty =
      if movementActions.sizeIs >= 10 then
        val avgLeft = movementActions.map(_.leftSpeed).sum / movementActions.size
        val avgRight = movementActions.map(_.rightSpeed).sum / movementActions.size
        val dl = abs(actLeft - avgLeft)
        val dr = abs(actRight - avgRight)
        if dl + dr > 0.5 then -(dl + dr) * 0.1 else 0.0
      else 0.0

    val forwardBonus =
      if abs(actLeft - actRight) < 0.5 && (actLeft.abs + actRight.abs) > 0.1 then 1.0 else 0.0

    spinPenalty + oscillationPenalty + forwardBonus
  end moveReward

  private def distanceFromObstacle(env: Environment, entity: Agent): Double =
    val agent =
      env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
    agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

end ExplorationReward
