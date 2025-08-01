package io.github.srs.model

import monix.eval.Task

final case class SimulationState(i: Int) extends ModelModule.State

object SimulationState:

  given IncrementLogic[SimulationState] with

    def increment(s: SimulationState): Task[SimulationState] =
      Task(s.copy(i = s.i + 1))
