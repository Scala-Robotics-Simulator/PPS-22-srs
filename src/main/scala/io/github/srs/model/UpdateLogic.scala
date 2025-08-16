package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.logic.*

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])

    def handleRobotAction(s: S, queue: Queue[IO, Event], robot: Robot, action: Action[IO])(using
        logic: RobotActionLogic[S],
    ): IO[S] =
      logic.handleRobotAction(s, queue, robot, action)

    def increment(s: S)(using logic: IncrementLogic[S]): IO[S] =
      logic.increment(s)

    def tick(s: S, delta: FiniteDuration)(using logic: TickLogic[S]): IO[S] =
      logic.tick(s, delta)

    def tickSpeed(s: S, speed: SimulationSpeed)(using logic: TickLogic[S]): IO[S] =
      logic.tickSpeed(s, speed)

    def pause(s: S)(using logic: PauseLogic[S]): IO[S] =
      logic.pause(s)

    def resume(s: S)(using logic: ResumeLogic[S]): IO[S] =
      logic.resume(s)

    def stop(s: S)(using logic: StopLogic[S]): IO[S] =
      logic.stop(s)
  end extension
end UpdateLogic
