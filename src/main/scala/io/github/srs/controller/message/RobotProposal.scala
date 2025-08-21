package io.github.srs.controller.message

import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action

/**
 * Represents a proposal for a robot to perform an action.
 * @param robot
 *   the robot that is being proposed to perform the action.
 * @param action
 *   the action that the robot is proposed to perform.
 */
final case class RobotProposal(robot: Robot, action: Action[IO])
