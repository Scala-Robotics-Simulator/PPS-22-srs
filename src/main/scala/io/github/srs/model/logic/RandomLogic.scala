package io.github.srs.model.logic

import cats.effect.IO
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.RNG

trait RandomLogic[S <: ModelModule.State]:
  def random(s: S, rng: RNG): IO[S]

object RandomLogic:

  given RandomLogic[SimulationState] with

    def random(s: SimulationState, rng: RNG): IO[SimulationState] =
      IO.pure(s.copy(simulationRNG = rng))
