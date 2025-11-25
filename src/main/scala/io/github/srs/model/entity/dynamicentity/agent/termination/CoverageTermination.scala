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
import io.github.srs.utils.SpatialUtils.{ countExplorableCells, discreteCell }

/**
 * Represents the state for an exploration-based termination condition. Tracks which cells in the environment have been
 * visited by an agent.
 *
 * @param visitedCells
 *   mutable set of visited cell coordinates represented as (x, y) tuples
 */
final case class ExplorationTerminationState(visitedCells: Set[(Int, Int)] = Set.empty)

private object ExplorationTerminationStateManager extends TerminationStateManager[Agent, ExplorationTerminationState]:
  /**
   * Creates an initial empty [[ExplorationTerminationState]].
   *
   * @return
   *   a new instance of [[ExplorationTerminationState]] with no visited cells.
   */
  override def createState(): ExplorationTerminationState = ExplorationTerminationState()

/**
 * A termination condition that ends an agent’s exploration when a given percentage of the environment has been visited.
 */
final case class CoverageTermination() extends StatefulTermination[Agent, ExplorationTerminationState]:

  private val logger = Logger(getClass.getName)

  override protected def stateManager: TerminationStateManager[Agent, ExplorationTerminationState] =
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
   *   the mutable [[ExplorationTerminationState]] being updated
   * @return
   *   a tuple (terminated: Boolean, updatedState: ExplorationState)
   */
  override def compute(
      prev: BaseState,
      current: BaseState,
      entity: Agent,
      action: Action[?],
      state: ExplorationTerminationState,
  ): (Boolean, ExplorationTerminationState) =
    // Determina la cella corrente dell'agente
    val currentCell = discreteCell(entity.position, CellSize)
    val isNewCell = !state.visitedCells.contains(currentCell)
    // Aggiorna lo stato delle celle visitate
    val updatedVisited = if isNewCell then state.visitedCells + currentCell else state.visitedCells
    // Numero di celle visitate
    val visitedCount = updatedVisited.size
    // Numero totale di celle esplorabili (tenendo conto di ostacoli e raggio agente)
    val totalExplorable = countExplorableCells(current.environment, entity.shape.radius, CellSize)
    // Numero di celle da visitare per raggiungere la soglia fissa (80%)
    val thresholdCells = (totalExplorable * CoverageThreshold).toInt
    // Determina se la soglia è stata raggiunta
    val reachedGoal = visitedCount >= thresholdCells

    val coveragePercent = visitedCount.toDouble / totalExplorable.toDouble * Percent
    logger.info(
      f"Exploration: $coveragePercent%.2f%% of explorable cells covered. " +
        f"Coverage: $visitedCount on $thresholdCells cells (total= $totalExplorable).",
    )
    logger.info(s"visitedCells: ${updatedVisited.toList}")

    (reachedGoal, state.copy(visitedCells = updatedVisited))
  end compute
end CoverageTermination
