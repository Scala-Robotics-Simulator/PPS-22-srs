package io.github.srs.model.logic

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.{ SimulationState, TickLogic }
import monix.eval.Task

object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): Task[SimulationState] =
      val remainingMillis = s.simulationTime.toMillis - delta.toMillis
      val clamped = if remainingMillis < 0 then 0 else remainingMillis
      Task.pure(s.copy(simulationTime = FiniteDuration(clamped, scala.concurrent.duration.MILLISECONDS)))
