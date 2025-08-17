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

    private def computeClosestSafePosition(currentRobot: Robot): Robot =
      currentRobot

    def handleRobotAction(
        s: SimulationState,
        queue: Queue[IO, Event],
        robot: Robot,
        action: Action[IO],
    ): IO[SimulationState] =

      def loop(currentState: SimulationState, currentRobot: Robot): IO[SimulationState] =
        println(s"${currentRobot.position}")
        val updatedEntities = updateEnvironment(currentState, robot, currentRobot)
        val validated = currentState.environment.copy(entities = updatedEntities).validate(insertBoundaries = false)
        validated match
          case Right(validEnv) =>
            IO.pure(currentState.copy(environment = validEnv))
          case Left(_) =>
            val safeRobot = computeClosestSafePosition(currentRobot)
            queue.offer(Event.CollisionDetected(queue, robot, safeRobot)) *>
              loop(currentState, safeRobot)

      for
        updatedRobot <- robot.applyMovementActions[IO](s.dt, action)
        newState <- loop(s, updatedRobot)
      yield newState
    end handleRobotAction

  end given
end RobotActionLogic
