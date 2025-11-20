package io.github.srs.model.entity.dynamicentity.agent.termination

import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CoverageTermination.{
  CellSize,
  CoverageThreshold,
  Percent,
}
import io.github.srs.utils.SpatialUtils.{ discreteCell, estimateRealCoverage }

/**
 * Represents the state for an exploration-based termination condition. Tracks which cells in the environment have been
 * visited by an agent.
 *
 * @param visitedCells
 *   mutable set of visited cell coordinates represented as (x, y) tuples
 */
final case class ExplorationState(visitedCells: Set[(Int, Int)] = Set.empty)

private object ExplorationTerminationStateManager extends TerminationStateManager[Agent, ExplorationState]:
  /**
   * Creates an initial empty [[ExplorationState]].
   *
   * @return
   *   a new instance of [[ExplorationState]] with no visited cells.
   */
  override def createState(): ExplorationState = ExplorationState()

/**
 * A termination condition that ends an agent’s exploration when a given percentage of the environment has been visited.
 *
 * @param coverageThreshold
 *   the fraction (0.0 to 1.0) of the total environment that must be covered to trigger termination (default: 0.8)
 * @param cellSize
 *   the size of each discretized cell in environment units (default: 1.0)
 */
final case class CoverageTermination() extends StatefulTermination[Agent, ExplorationState]:

  private val logger = Logger(getClass.getName)

  override protected def stateManager: TerminationStateManager[Agent, ExplorationState] =
    ExplorationTerminationStateManager

  /**
   * Computes whether the exploration termination condition has been met.
   *
   * @param prev
   *   previous simulation state
   * @param current
   *   current simulation state
   * @param entity
   *   the agent whose termination condition is being evaluated
   * @param action
   *   the action taken by the agent
   * @param state
   *   the mutable [[ExplorationState]] being updated
   * @return
   *   a tuple (terminated: Boolean, updatedState: ExplorationState)
   */
  override def compute(
      prev: BaseState,
      current: BaseState,
      entity: Agent,
      action: Action[?],
      state: ExplorationState,
  ): (Boolean, ExplorationState) =

    // exploration new cell
    val currentCell = discreteCell(entity.position, CellSize)
    val isNewCell = !state.visitedCells.contains(currentCell)
    val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
//    val coverage = estimateCoverage(updatedVisited, current.environment, CellSize)
    val coverage = estimateRealCoverage(updatedVisited, current.environment, entity.shape.radius, CellSize)

//    val reachedGoalQL = coverage >= CoverageThreshold
//    val coverageThreshold = explorableThreshold(current.environment, entity.shape.radius, CellSize, 0.8)
    val reachedGoalDQN = coverage >= CoverageThreshold
    logger.info(f"Exploration: ${coverage * Percent}%.2f%% area covered.")
    logger.info(s"visitedCells: ${updatedVisited.toList.toString()}")

    val shouldTerminate = reachedGoalDQN
    val newState = state.copy(visitedCells = updatedVisited)
    (shouldTerminate, newState)
  end compute

end CoverageTermination
