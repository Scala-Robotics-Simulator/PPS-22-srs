package io.github.srs.model.logic

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.Event
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.{ ModelModule, SimulationState }

trait CollisionLogic[S <: ModelModule.State]:

  def handleCollision(
      state: S,
      queue: Queue[IO, Event],
      robot: Robot,
      updatedRobot: Robot,
  ): IO[S]

object CollisionLogic:

  given CollisionLogic[SimulationState] with

    def handleCollision(
        state: SimulationState,
        queue: Queue[IO, Event],
        robot: Robot,
        updatedRobot: Robot,
    ): IO[SimulationState] = IO.pure(state)
