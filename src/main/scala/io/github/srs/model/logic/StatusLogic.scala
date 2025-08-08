package io.github.srs.model.logic

import io.github.srs.model.SimulationConfig.SimulationStatus
import io.github.srs.model.{ ModelModule, SimulationState }
import cats.effect.IO

trait PauseLogic[S <: ModelModule.State]:
  def pause(s: S): IO[S]

trait ResumeLogic[S <: ModelModule.State]:
  def resume(s: S): IO[S]

trait StopLogic[S <: ModelModule.State]:
  def stop(s: S): IO[S]

object StatusLogic:

  given pauseLogic: PauseLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.PAUSED))

  given resumeLogic: ResumeLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.RUNNING))

  given stopLogic: StopLogic[SimulationState] = (s: SimulationState) =>
    IO.pure(s.copy(simulationStatus = SimulationStatus.STOPPED))
