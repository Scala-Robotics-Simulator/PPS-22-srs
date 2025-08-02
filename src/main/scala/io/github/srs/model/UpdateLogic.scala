package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import monix.eval.Task

trait ChangeTimeLogic[S <: ModelModule.State]:
  def changeTime(s: S, simulationTime: FiniteDuration): Task[S]

trait IncrementLogic[S <: ModelModule.State]:
  def increment(s: S): Task[S]

trait TickLogic[S <: ModelModule.State]:
  def tick(s: S, delta: FiniteDuration): Task[S]

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])

    def changeTime(s: S, simulationTime: FiniteDuration)(using logic: ChangeTimeLogic[S]): Task[S] =
      logic.changeTime(s, simulationTime)
    def increment(s: S)(using logic: IncrementLogic[S]): Task[S] = logic.increment(s)
    def tick(s: S, delta: FiniteDuration)(using logic: TickLogic[S]): Task[S] = logic.tick(s, delta)
