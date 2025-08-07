package io.github.srs.model.logic

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.{ ModelModule, SimulationState }
import monix.eval.Task

trait TickLogic[S <: ModelModule.State]:
  def tick(s: S, delta: FiniteDuration): Task[S]
  def tickSpeed(s: S, speed: SimulationSpeed): Task[S]

object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): Task[SimulationState] =
      val updatedElapsed = s.elapsedTime + delta
      Task.pure(s.copy(elapsedTime = updatedElapsed))

    def tickSpeed(s: SimulationState, speed: SimulationSpeed): Task[SimulationState] =
      Task.pure(s.copy(simulationSpeed = speed))
