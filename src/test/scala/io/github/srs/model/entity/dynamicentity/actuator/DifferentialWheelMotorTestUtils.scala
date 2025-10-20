package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialKinematics.{
  computePositionAndOrientation,
  computeVelocities,
  computeWheelVelocities,
}
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.{ at, withOrientation }
import io.github.srs.model.entity.dynamicentity.robot.Robot

object DifferentialWheelMotorTestUtils:

  def calculateMovement(dt: FiniteDuration, robot: Robot): (Point2D, Orientation) =
    robot.actuators.collectFirst { case wm: DifferentialWheelMotor[Robot] => wm } match
      case Some(wm) =>
        import Point2D.*
        val wheelDistance = robot.shape.radius * 2
        val theta = robot.orientation.toRadians

        val expectedRobot = (
          computeWheelVelocities
            andThen computeVelocities(wheelDistance)
            andThen computePositionAndOrientation(theta, dt)
            andThen { case (dx, dy, newOrientation) =>
              val newPos = Point2D(robot.position.x + dx, robot.position.y + dy)
              robot at newPos withOrientation newOrientation
            }
        )(wm)
        (expectedRobot.position, expectedRobot.orientation)
      case None => (robot.position, robot.orientation)
end DifferentialWheelMotorTestUtils
