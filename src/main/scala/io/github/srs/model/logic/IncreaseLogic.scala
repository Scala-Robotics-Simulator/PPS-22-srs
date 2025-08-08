package io.github.srs.model.logic

import io.github.srs.model.{ ModelModule, SimulationState }
import cats.effect.IO

trait IncrementLogic[S <: ModelModule.State]:
  def increment(s: S): IO[S]

object IncreaseLogic:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): IO[SimulationState] =
      IO(s.copy(i = s.i + 1))
