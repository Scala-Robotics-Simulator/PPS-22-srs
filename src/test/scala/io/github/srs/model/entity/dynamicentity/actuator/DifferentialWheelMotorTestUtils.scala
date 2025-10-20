package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialKinematics.{
  computePositionAndOrientation,
  computeVelocities,
  computeWheelVelocities,
}
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.Point2D.*

object DifferentialWheelMotorTestUtils:

  def calculateMovement(dt: FiniteDuration, robot: Robot): (Point2D, Orientation) =
    robot.actuators.collectFirst { case wm: DifferentialWheelMotor[Robot] => wm } match
      case Some(wm) =>
        val wheelDistance = robot.shape.radius * 2
        val theta = robot.orientation.toRadians

        val (dx, dy, newOrientation) =
          (computeWheelVelocities // wheel speed * wheel radius
            andThen computeVelocities(wheelDistance) // -> linear v, angular ω
            andThen computePositionAndOrientation(theta, dt) // -> dx, dy, new θ
          )(wm)

        val newPos = Point2D(robot.position.x + dx, robot.position.y + dy)
        (newPos, newOrientation)

      case None =>
        (robot.position, robot.orientation)
