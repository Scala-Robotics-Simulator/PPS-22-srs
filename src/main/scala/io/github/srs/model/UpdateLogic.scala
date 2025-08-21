package io.github.srs.model

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import io.github.srs.controller.message.RobotProposal
import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.logic.LogicsBundle
import io.github.srs.utils.random.RNG

object UpdateLogic:

  extension [S <: ModelModule.State](m: Model[S])

    def handleRobotActionsProposals(s: S, proposals: List[RobotProposal])(using
        bundle: LogicsBundle[S],
    ): IO[S] =
      bundle.robotActions.handleRobotActionsProposals(s, proposals)

    def tick(s: S, delta: FiniteDuration)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.tick.tick(s, delta)

    def tickSpeed(s: S, speed: SimulationSpeed)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.tick.tickSpeed(s, speed)

    def random(s: S, rng: RNG)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.random.random(s, rng)

    def pause(s: S)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.pause.pause(s)

    def resume(s: S)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.resume.resume(s)

    def stop(s: S)(using bundle: LogicsBundle[S]): IO[S] =
      bundle.stop.stop(s)
  end extension
end UpdateLogic
