package io.github.srs.model.logic

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.{ ModelModule, SimulationState }
import cats.effect.IO

trait TickLogic[S <: ModelModule.State]:
  def tick(s: S, delta: FiniteDuration): IO[S]
  def tickSpeed(s: S, speed: SimulationSpeed): IO[S]

object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): IO[SimulationState] =
      val updatedElapsed = s.elapsedTime + delta
      IO.pure(s.copy(elapsedTime = updatedElapsed))

    def tickSpeed(s: SimulationState, speed: SimulationSpeed): IO[SimulationState] =
      IO.pure(s.copy(simulationSpeed = speed))
