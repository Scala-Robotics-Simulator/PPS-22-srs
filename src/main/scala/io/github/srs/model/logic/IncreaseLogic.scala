package io.github.srs.model.logic

import io.github.srs.model.{ IncrementLogic, SimulationState }
import monix.eval.Task

object IncreaseLogic:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): Task[SimulationState] =
      Task(s.copy(i = s.i + 1))
