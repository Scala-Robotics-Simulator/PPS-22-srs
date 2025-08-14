package io.github.srs.model.logic

import io.github.srs.model.ModelModule
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action

trait RobotActionLogic[S <: ModelModule.State]:
  def handleRobotAction(s: S, robot: Robot, action: Action[?]): S

object RobotActionLogic:

  given RobotActionLogic[ModelModule.State] with

    // TODO: dummy implementation, replace with actual logic
    def handleRobotAction(s: ModelModule.State, robot: Robot, action: Action[?]): ModelModule.State = s
