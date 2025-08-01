package io.github.srs.model

import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import monix.eval.Task

trait IncrementLogic[S <: ModelModule.State]:
  def increment(s: S): Task[S]

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])
    def increment(s: S)(using logic: IncrementLogic[S]): Task[S] = logic.increment(s)
