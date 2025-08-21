package io.github.srs.controller.message

import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action

final case class RobotProposal(robot: Robot, action: Action[IO])
