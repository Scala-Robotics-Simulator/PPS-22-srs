package io.github.srs.model.entity.dynamicentity.agent.termination

import scala.collection.mutable

import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.utils.SpatialUtils.{ discreteCell, estimateCoverage }

/**
 * Represents the state for an exploration-based termination condition. Tracks which cells in the environment have been
 * visited by an agent.
 *
 * @param visitedCells
 *   mutable set of visited cell coordinates represented as (x, y) tuples
 */
final case class ExplorationState(
    visitedCells: mutable.Set[(Int, Int)] = mutable.Set(),
)

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
final case class ExplorationCoverageTermination(
    coverageThreshold: Double = 0.8,
    cellSize: Double = 0.5, // 1,
) extends StatefulTermination[Agent, ExplorationState]:

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

    val currentCell = discreteCell(entity.position, cellSize)
    state.visitedCells += currentCell
    logger.info(s"visitedCells: ${state.visitedCells.toList.toString()}")

    val coverage = estimateCoverage(state.visitedCells, current.environment, cellSize)
    logger.info(f"Exploration: ${coverage * 100}%.2f%% area covered.")

    (coverage >= coverageThreshold, state)

end ExplorationCoverageTermination
