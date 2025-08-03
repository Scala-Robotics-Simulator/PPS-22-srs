package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.logic.{ IncrementLogic, TickLogic }
import monix.eval.Task

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])

    def increment(s: S)(using logic: IncrementLogic[S]): Task[S] =
      logic.increment(s)

    def tick(s: S, delta: FiniteDuration)(using logic: TickLogic[S]): Task[S] =
      logic.tick(s, delta)
