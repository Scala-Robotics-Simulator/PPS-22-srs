package io.github.srs.model.logic

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.Event
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.*
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_Robot_Robot

trait RobotActionLogic[S <: ModelModule.State]:
  def handleRobotAction(s: S, queue: Queue[IO, Event], robot: Robot, action: Action[IO]): IO[S]

object RobotActionLogic:

  given RobotActionLogic[SimulationState] with

    private def updateEnvironment(s: SimulationState, robot: Robot, updatedRobot: Robot): Set[Entity] =
      s.environment.entities.map:
        case r: Robot if r == robot => updatedRobot
        case e => e

    def handleRobotAction(
        s: SimulationState,
        queue: Queue[IO, Event],
        robot: Robot,
        action: Action[IO],
    ): IO[SimulationState] =
      for
        updatedRobot <- robot.applyMovementActions[IO](s.dt, action)
        updatedEntities = updateEnvironment(s, robot, updatedRobot)
        validated = s.environment.copy(entities = updatedEntities).validate(insertBoundaries = false)
        newState <- validated match
          case Right(validEnv) =>
            IO.pure(s.copy(environment = validEnv))
          case Left(_) =>
            queue.offer(Event.CollisionDetected(s, robot, updatedRobot)) *> IO.pure(s)
      yield newState
  end given
end RobotActionLogic
