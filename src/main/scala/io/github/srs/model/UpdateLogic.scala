package io.github.srs.model

import io.github.srs.model.SimulationState.SimulationState
import monix.eval.Task

object UpdateLogic:

  def increment(s: SimulationState): Task[SimulationState] =
    Task(s.copy(i = s.i + 1))
