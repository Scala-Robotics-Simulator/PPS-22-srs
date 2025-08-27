package io.github.srs.model.logic

import io.github.srs.model.ModelModule.State
import io.github.srs.model.SimulationState
import io.github.srs.model.logic.StatusLogic.given
import io.github.srs.model.logic.TimeLogic.given
import io.github.srs.model.logic.RandomLogic.given
import io.github.srs.model.logic.RobotActionsLogic.given

/**
 * A bundle of all logic traits for a given state type.
 * @tparam S
 *   the type of the state.
 */
trait LogicsBundle[S <: State]:
  def tickLogic: TickLogic[S]
  def randomLogic: RandomLogic[S]
  def pauseLogic: PauseLogic[S]
  def resumeLogic: ResumeLogic[S]
  def stopLogic: StopLogic[S]
  def robotActionsLogic: RobotActionsLogic[S]

/**
 * Companion object for [[LogicsBundle]] containing given instances.
 */
given simulationStateLogicsBundle: LogicsBundle[SimulationState] with
  val tickLogic: TickLogic[SimulationState] = summon[TickLogic[SimulationState]]
  val randomLogic: RandomLogic[SimulationState] = summon[RandomLogic[SimulationState]]
  val pauseLogic: PauseLogic[SimulationState] = summon[PauseLogic[SimulationState]]
  val resumeLogic: ResumeLogic[SimulationState] = summon[ResumeLogic[SimulationState]]
  val stopLogic: StopLogic[SimulationState] = summon[StopLogic[SimulationState]]
  val robotActionsLogic: RobotActionsLogic[SimulationState] = summon[RobotActionsLogic[SimulationState]]
