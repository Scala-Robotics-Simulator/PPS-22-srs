package io.github.srs.model.logic

import io.github.srs.model.SimulationConfig.SimulationStatus
import io.github.srs.model.{ ModelModule, SimulationState }
import cats.effect.IO

/**
 * Logic for handling simulation status changes: pause.
 * @tparam S
 *   the type of the simulation state.
 */
trait PauseLogic[S <: ModelModule.State]:
  /**
   * Pauses the simulation by updating its status.
   * @param s
   *   the current simulation state.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with status set to [[SimulationStatus.PAUSED]].
   */
  def pause(s: S): IO[S]

/**
 * Logic for handling simulation status changes: resume.
 * @tparam S
 *   the type of the simulation state.
 */
trait ResumeLogic[S <: ModelModule.State]:
  /**
   * Resumes the simulation by updating its status.
   * @param s
   *   the current simulation state.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with status set to [[SimulationStatus.RUNNING]].
   */
  def resume(s: S): IO[S]

/**
 * Logic for handling simulation status changes: stop.
 * @tparam S
 *   the type of the simulation state.
 */
trait StopLogic[S <: ModelModule.State]:
  /**
   * Stops the simulation by updating its status.
   * @param s
   *   the current simulation state.
   * @return
   *   an [[IO]] effect that produces the updated simulation state with status set to [[SimulationStatus.STOPPED]].
   */
  def stop(s: S): IO[S]

/**
 * Companion object for status logic traits containing given instances.
 */
object StatusLogic:

  given pauseLogic: PauseLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.PAUSED))

  given resumeLogic: ResumeLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.RUNNING))

  given stopLogic: StopLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.STOPPED))
