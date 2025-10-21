package io.github.srs.model.logic

import io.github.srs.model.ModelModule.State
import io.github.srs.model.SimulationState
import io.github.srs.model.logic.StatusLogic.given
import io.github.srs.model.logic.TimeLogic.given
import io.github.srs.model.logic.RandomLogic.given
import io.github.srs.model.logic.DynamicEntityActionsLogic.given
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.BaseSimulationState

trait BaseLogicsBunldle[S <: BaseState]:
  def tickLogic: BaseTickLogic[S]
  def randomLogic: RandomLogic[S]
  def dynamicEntityActionsLogic: DynamicEntityActionsLogic[S]

/**
 * A bundle of all logic traits for a given state type.
 * @tparam S
 *   the type of the state.
 */
trait LogicsBundle[S <: State] extends BaseLogicsBunldle[S]:
  override def tickLogic: TickLogic[S]
  def pauseLogic: PauseLogic[S]
  def resumeLogic: ResumeLogic[S]
  def stopLogic: StopLogic[S]
  def dynamicEntityActionsLogic: DynamicEntityActionsLogic[S]

/**
 * Companion object for [[LogicsBundle]] containing given instances.
 */
given simulationStateLogicsBundle: LogicsBundle[SimulationState] with
  val tickLogic: TickLogic[SimulationState] = summon[TickLogic[SimulationState]]
  val randomLogic: RandomLogic[SimulationState] = summon[RandomLogic[SimulationState]]
  val pauseLogic: PauseLogic[SimulationState] = summon[PauseLogic[SimulationState]]
  val resumeLogic: ResumeLogic[SimulationState] = summon[ResumeLogic[SimulationState]]
  val stopLogic: StopLogic[SimulationState] = summon[StopLogic[SimulationState]]

  val dynamicEntityActionsLogic: DynamicEntityActionsLogic[SimulationState] =
    summon[DynamicEntityActionsLogic[SimulationState]]

trait RLLogicsBundle[S <: BaseState] extends BaseLogicsBunldle[S]:
  def stateLogic: StateLogic[S]

given rlLogicsBundle: RLLogicsBundle[BaseSimulationState] with
  val tickLogic: BaseTickLogic[BaseSimulationState] = summon[BaseTickLogic[BaseSimulationState]]
  val randomLogic: RandomLogic[BaseSimulationState] = summon[RandomLogic[BaseSimulationState]]

  def dynamicEntityActionsLogic: DynamicEntityActionsLogic[BaseSimulationState] =
    summon[DynamicEntityActionsLogic[BaseSimulationState]]
  val stateLogic: StateLogic[BaseSimulationState] = summon[StateLogic[BaseSimulationState]]
