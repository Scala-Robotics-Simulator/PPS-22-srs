package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.logic.{ IncrementLogic, TickLogic }
import monix.eval.Task
import io.github.srs.model.logic.{ PauseLogic, ResumeLogic, StopLogic }

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])

    def increment(s: S)(using logic: IncrementLogic[S]): Task[S] =
      logic.increment(s)

    def tick(s: S, delta: FiniteDuration)(using logic: TickLogic[S]): Task[S] =
      logic.tick(s, delta)

    def tickSpeed(s: S, speed: SimulationSpeed)(using logic: TickLogic[S]): Task[S] =
      logic.tickSpeed(s, speed)

    def pause(s: S)(using logic: PauseLogic[S]): Task[S] =
      logic.pause(s)

    def resume(s: S)(using logic: ResumeLogic[S]): Task[S] =
      logic.resume(s)

    def stop(s: S)(using logic: StopLogic[S]): Task[S] =
      logic.stop(s)
end UpdateLogic
