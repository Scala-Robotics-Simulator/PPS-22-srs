package io.github.srs.model.logic

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.model.{ ModelModule, SimulationState }
import monix.eval.Task

trait TickLogic[S <: ModelModule.State]:
  def tick(s: S, delta: FiniteDuration): Task[S]

object TimeLogic:

  given TickLogic[SimulationState] with

    def tick(s: SimulationState, delta: FiniteDuration): Task[SimulationState] =
      val remainingMillis = s.simulationTime.toMillis - delta.toMillis
      val clamped = if remainingMillis < 0 then 0 else remainingMillis
      Task.pure(s.copy(simulationTime = FiniteDuration(clamped, MILLISECONDS)))
