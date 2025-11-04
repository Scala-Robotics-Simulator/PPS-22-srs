package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.collection.mutable

import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_T_T
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import io.github.srs.utils.SpatialUtils.{ discreteCell, estimateCoverage }
import utils.types.CircularBuffer

object ExplorationReward:

  case class ExplorationState(
      var ticks: Int = 0,
      positions: CircularBuffer[Point2D] = CircularBuffer(200),
      actionHistory: CircularBuffer[Action[?]] = CircularBuffer(200),
      visitedCells: mutable.Set[(Int, Int)] = mutable.Set(),
      achievedMilestones: mutable.Set[Double] = mutable.Set(),
      var explorationComplete: Boolean = false,
      maxTicks: Int = 10_000,
  )

  private object ExplorationRewardStateManager extends RewardStateManager[Agent, ExplorationState]:
    override def createState(): ExplorationState = ExplorationState()

  final case class Exploration(cellSize: Double = 0.5, coverageScaling: Double = 100.0)
      extends StatefulReward[Agent, ExplorationState]:
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

      val newTick = state.ticks + 1
      val updatedPositions = state.positions.add(entity.position)

      // stuck
      val stuckWindow = 10
      val isStuck =
        if updatedPositions.sizeIs >= stuckWindow then
          val recentPositions = updatedPositions.takeRight(stuckWindow)
          recentPositions.headOption.exists(head => recentPositions.forall(_ == head))
        else false
      val stuckReward = if isStuck then -1.0 else +0.5

      // collide
      val currentMin = distanceFromObstacle(current.environment, entity)
      val collidingReward = if currentMin < CollisionTriggerDistance then -100.0 else 0.0

      // bonus milestone coverage
      val currentCell = discreteCell(entity.position, cellSize)
      state.visitedCells += currentCell
      val coverage = estimateCoverage(state.visitedCells, current.environment, cellSize)
      val milestones = (1 to 100).map(_ * 0.1) // 1%, 2%, ..., 100%
      val eps = 1e-6
      val milestoneReward = +50.0
      val newMilestones = milestones.filter { m =>
        coverage + eps >= m && !state.achievedMilestones.exists(am => math.abs(am - m) < eps)
      }
      newMilestones.foreach(state.achievedMilestones.add)
      val milestoneBonus = newMilestones.size * milestoneReward
      val updateAchieved = state.achievedMilestones ++ newMilestones

      // final bonus
      val finalCoverageThreshold = 0.8
      val finalReward = +500
      val completionBonus: Double =
        if !state.explorationComplete && coverage >= finalCoverageThreshold then
          state.explorationComplete = true
          finalReward
        else 0.0

      val isNewCell = !state.visitedCells.contains(currentCell)
      val explorationReward = if isNewCell then +5.0 else 0.0

      val reward =
        milestoneBonus + // +50.0 una volta a ogni milestone raggiunta oppure 0.0
          completionBonus + // +500.0 oppure 0.0
          collidingReward + // -100.0 oppure 0.0
          stuckReward + // -1.0 oppure 0.5
          explorationReward // +5.0 oppure 0.0

      logger.info(
        f"TICK [$newTick] colliding=$collidingReward stuck=$stuckReward coverage=$coverage " +
          f"milestone=$milestoneBonus | reward=$reward ",
//          f"milestones=${state.achievedMilestones.toList.sorted.mkString(",")} " +
//          f"final=${state.explorationComplete}"
      )

      val newState = state.copy(
        ticks = newTick,
        positions = updatedPositions,
        actionHistory = state.actionHistory.add(action),
        visitedCells = state.visitedCells,
        achievedMilestones = updateAchieved,
        explorationComplete = state.explorationComplete,
        maxTicks = current.simulationTime.map(st => (st.toMillis / current.dt.toMillis).toInt).getOrElse(10_000),
      )
      (reward, newState)

    end compute

    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      val agent =
        env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
      agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => math.min(acc, sr.value))
  end Exploration
end ExplorationReward
