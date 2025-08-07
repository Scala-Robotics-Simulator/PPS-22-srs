package io.github.srs.model.logic

import io.github.srs.model.{ ModelModule, SimulationState }
import monix.eval.Task

trait IncrementLogic[S <: ModelModule.State]:
  def increment(s: S): Task[S]

object IncreaseLogic:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): Task[SimulationState] =
      Task(s.copy(i = s.i + 1))
