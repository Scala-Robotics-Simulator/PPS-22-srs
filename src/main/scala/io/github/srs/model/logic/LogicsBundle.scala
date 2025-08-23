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
  def tick: TickLogic[S]
  def random: RandomLogic[S]
  def pause: PauseLogic[S]
  def resume: ResumeLogic[S]
  def stop: StopLogic[S]
  def robotActions: RobotActionsLogic[S]
  def illumination: IlluminationLogic[S]

/**
 * Companion object for [[LogicsBundle]] containing given instances.
 */
given simulationStateLogicsBundle: LogicsBundle[SimulationState] with
  val tick: TickLogic[SimulationState] = summon[TickLogic[SimulationState]]
  val random: RandomLogic[SimulationState] = summon[RandomLogic[SimulationState]]
  val pause: PauseLogic[SimulationState] = summon[PauseLogic[SimulationState]]
  val resume: ResumeLogic[SimulationState] = summon[ResumeLogic[SimulationState]]
  val stop: StopLogic[SimulationState] = summon[StopLogic[SimulationState]]
  val robotActions: RobotActionsLogic[SimulationState] = summon[RobotActionsLogic[SimulationState]]
  val illumination: IlluminationLogic[SimulationState] = summon[IlluminationLogic[SimulationState]]
