package io.github.srs.model.logic

import cats.effect.IO
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action

trait RobotActionLogic[S <: ModelModule.State]:
  def handleRobotAction(s: S, robot: Robot, action: Action[?]): IO[S]

object RobotActionLogic:

  given RobotActionLogic[SimulationState] with

    // TODO: dummy implementation, replace with actual logic
    def handleRobotAction(s: SimulationState, robot: Robot, action: Action[?]): IO[SimulationState] = IO(s)
//      IO(s.copy(environment = s.environment.updated(robot.id, robot.copy(actions = robot.actions :+ action)))))
