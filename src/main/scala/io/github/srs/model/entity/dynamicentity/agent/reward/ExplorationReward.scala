package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.exp

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
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_T_T
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CoverageTermination.*
import io.github.srs.utils.SpatialUtils.{ discreteCell, estimateCoverage, estimateRealCoverage }
import utils.types.CircularBuffer

object ExplorationReward:

  private val logger = Logger(getClass.getName)

  case class ExplorationState(
      var ticks: Int = 0,
      positions: List[Point2D] = List.empty,
      actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      visitedCells: Set[(Int, Int)] = Set.empty,
      achievedMilestones: Set[Int] = Set.empty,
      lastCoverage: Double = 0.0,
      maxTicks: Int = 10_000,
  )

  private object ExplorationRewardStateManager extends RewardStateManager[Agent, ExplorationState]:
    override def createState(): ExplorationState = ExplorationState()

  final case class ExplorationQL() extends StatefulReward[Agent, ExplorationState]:

    private val StuckPenalty: Double = -1.0
    private val NotStuckBonus: Double = +0.5
    private val CollidingPenalty: Double = -100.0
    private val MilestoneBonus: Double = +5.0
    private val ExplorationBonus: Double = +5.0
    private val CoverageBonus: Double = +500.0
    private val NotBonus: Double = 0.0

    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
      ExplorationRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ExplorationState,
    ): (Double, ExplorationState) =

      val newTick = state.ticks + 1
      val updatedPositions = entity.position :: state.positions

      // stuck
      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
      val stuckReward = if isStuck then StuckPenalty else NotStuckBonus

      // collision
      val currentMin = distanceFromObstacle(current.environment, entity)
      val collidingReward = if currentMin < CollisionTriggerDistance then CollidingPenalty else NotBonus

      // exploration new cell
      val currentCell = discreteCell(entity.position, CellSize)
      val isNewCell = !state.visitedCells.contains(currentCell)
      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
      val explorationReward = if isNewCell then ExplorationBonus else NotBonus

      // bonus milestone coverage
      val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
      val currentPercent = math.floor(coverage * Percent).toInt
      val achieved = state.achievedMilestones
      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
      val milestonesReward = newMilestones.map(m => m * MilestoneBonus).sum
      val updateMilestones = achieved ++ newMilestones

      // final bonus
      val completionReward: Double = if coverage >= CoverageThreshold then CoverageBonus else NotBonus

      val reward =
        milestonesReward + // +5.0 * milestone.size or 0.0
          completionReward + // +500.0 or 0.0
          collidingReward + // -100.0 or 0.0
          stuckReward + // -1.0 or 0.5
          explorationReward // +5.0 or 0.0

      logger.info(
        f"TICK [$newTick] colliding=$collidingReward stuck=$stuckReward coverage=$coverage exploration=$explorationReward " +
          f"milestone=$milestonesReward completion=$completionReward | reward=$reward ",
      )

      val newState = state.copy(
        ticks = newTick,
        actionHistory = state.actionHistory.add(action),
        positions = updatedPositions,
        visitedCells = updatedVisited,
        achievedMilestones = updateMilestones,
        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
      )
      (reward, newState)
    end compute
  end ExplorationQL

  final case class ExplorationDQN() extends StatefulReward[Agent, ExplorationState]:

    private val StuckPenalty: Double = -1.0
    //    private val NotStuckBonus: Double = +0.5
    private val CollisionHardPenalty = -100.0
    // private val CollisionSoftPenalty = -1.0
    private val MilestoneBonus: Double = +20.0
    private val ExplorationBonus: Double = +30.0
    private val CoverageBonus: Double = +500.0
    private val NotBonus: Double = 0.0

    override protected def stateManager: RewardStateManager[Agent, ExplorationState] =
      ExplorationRewardStateManager

    override def compute(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
        state: ExplorationState,
    ): (Double, ExplorationState) =

      val newTick = state.ticks + 1
      val updatedPositions = entity.position :: state.positions

      val isStuck = isAgentStuck(updatedPositions, WindowStuck)
      val stuckReward = if isStuck then StuckPenalty else NotBonus

      val currentCell = discreteCell(entity.position, CellSize)
      val isNewCell = !state.visitedCells.contains(currentCell)
      val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells

      val explorationReward = if isNewCell then ExplorationBonus else NotBonus

      val coverage = estimateRealCoverage(updatedVisited, current.environment, entity.shape.radius, CellSize)

      val currentPercent = math.floor(coverage * Percent).toInt
      val achieved = state.achievedMilestones
      val newMilestones = (1 to currentPercent).filterNot(achieved.contains).toSet
      val milestonesReward = newMilestones.map(m => (m.toDouble / Percent) * MilestoneBonus).sum
      val updateMilestones = achieved ++ newMilestones

      val completionReward: Double =
        if coverage >= CoverageThreshold then
          val scaled = (coverage - CoverageThreshold) / (1.0 - CoverageThreshold)
          CoverageBonus * math.min(1.0, math.max(0.0, scaled))
        else NotBonus

      val currentMin = distanceFromObstacle(current.environment, entity)
      val collidingReward = if currentMin < CollisionTriggerDistance then CollisionHardPenalty else NotBonus
      val rClear = clearanceReward(prev.environment, current.environment, entity, -5)
      val prevAgent = getAgentFromId(entity, prev)
      val rMove = moveReward(entity, prevAgent, action, currentMin)

      val sensorVariance = variance(entity.senseAll[Id](current.environment).proximityReadings.map(_.value))
      val curiosityReward = sensorVariance * 0.5

      val rawReward =
        stuckReward +
          explorationReward +
          milestonesReward +
          completionReward +
          (collidingReward + rClear + rMove).max(-20).min(+20) +
          curiosityReward

      val reward = rawReward.max(-1.0).min(+1.0)

      logger.info(
        f"TICK [$newTick] reward=$reward | " +
          f"colliding=$collidingReward " +
          f"stuck=$stuckReward " +
          f"coverage=$coverage " +
          f"exploration=$explorationReward " +
          f"milestone=$milestonesReward " +
          f"completion=$completionReward " +
          f"move=$rMove " +
          f"clearance=$rClear ",
      )

      val newState = state.copy(
        ticks = newTick,
        actionHistory = state.actionHistory.add(action),
        positions = updatedPositions,
        visitedCells = updatedVisited,
        achievedMilestones = updateMilestones,
        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
      )
      (reward, newState)
    end compute

    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)

  end ExplorationDQN

  private def moveReward(agent: Agent, prevAgent: Agent, action: Action[?], currentMin: Double): Double =
    if agent.position == prevAgent.position then if currentMin < 0.1 then 0.5 else -1.0
    else
      action match
        case ma: MovementAction[?] if ma.leftSpeed == 0.0 || ma.rightSpeed == 0.0 =>
          if currentMin > 0.5 then -1.0 else 1.0
        case ma: MovementAction[?] if ma.leftSpeed > 0 && ma.rightSpeed > 0 => 2.0
        case _ => -1.0

  private def clearanceReward(prev: Environment, current: Environment, entity: Agent, base: Double): Double =
    val prevMin = distanceFromObstacle(prev, entity)
    val currMin = distanceFromObstacle(current, entity)
    val delta = currMin - prevMin
    val rChange = delta * 10.0
    val rProximity = -exp(base * currMin)
    (rChange + rProximity) / 2

  private def distanceFromObstacle(env: Environment, entity: Agent): Double =
    val agent =
      env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
    agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => math.min(acc, sr.value))

  private def isAgentStuck(positions: List[Point2D], window: Int, tolerance: Double = 0.01): Boolean =
    if positions.sizeIs >= window then
      val recentPositions = positions.take(window)
      recentPositions.headOption.exists { head =>
        recentPositions.forall(p => head.distanceTo(p) < tolerance)
      }
    else false

  def variance(xs: Seq[Double]): Double =
    if xs.isEmpty then 0.0
    else
      val mean = xs.sum / xs.size
      val meanSq = xs.map(x => x * x).sum / xs.size
      meanSq - mean * mean

end ExplorationReward
