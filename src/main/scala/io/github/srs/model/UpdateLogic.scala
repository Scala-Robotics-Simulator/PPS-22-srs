package io.github.srs.model

import io.github.srs.model.SimulationState.SimulationState

object UpdateLogic:

  private val MaxIterations = 10_000_000

  def increment(s: SimulationState): Option[SimulationState] =
    if s.i >= MaxIterations then None
    else Some(s.copy(i = s.i + 1))
