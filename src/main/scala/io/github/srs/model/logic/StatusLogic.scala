package io.github.srs.model.logic

import io.github.srs.model.SimulationConfig.SimulationStatus
import io.github.srs.model.{ ModelModule, SimulationState }
import monix.eval.Task

trait PauseLogic[S <: ModelModule.State]:
  def pause(s: S): Task[S]

trait ResumeLogic[S <: ModelModule.State]:
  def resume(s: S): Task[S]

trait StopLogic[S <: ModelModule.State]:
  def stop(s: S): Task[S]

object StatusLogic:

  given pauseLogic: PauseLogic[SimulationState] = (s: SimulationState) =>
    Task.pure(s.copy(simulationStatus = SimulationStatus.PAUSED))

  given resumeLogic: ResumeLogic[SimulationState] = (s: SimulationState) =>
    Task.pure(s.copy(simulationStatus = SimulationStatus.RUNNING))

  given stopLogic: StopLogic[SimulationState] = (s: SimulationState) =>
    Task.pure(s.copy(simulationStatus = SimulationStatus.STOPPED))
