package io.github.srs.model

import monix.eval.Task

object SimulationLogic:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): Task[SimulationState] =
      Task(s.copy(i = s.i + 1))
