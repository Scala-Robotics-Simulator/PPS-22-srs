package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.entity.*

object WheelMotorTestUtils:

  def calculateMovement(dt: FiniteDuration, robot: Robot): (Point2D, Orientation) =
    robot.actuators.collectFirst { case wm: WheelMotor => wm } match
      case Some(wm) =>
        import Point2D.*
        val vLeft = wm.left.speed * wm.left.shape.radius
        val vRight = wm.right.speed * wm.right.shape.radius
        val velocity = (vLeft + vRight) / 2
        val theta = robot.orientation.toRadians
        val wheelDistance = robot.shape.radius * 2
        val omega = (vRight - vLeft) / wheelDistance
        val dx = velocity * math.cos(theta) * dt.toSeconds
        val dy = velocity * math.sin(theta) * dt.toSeconds
        (
          Point2D(robot.position.x + dx, robot.position.y + dy),
          Orientation.fromRadians(theta + omega * dt.toSeconds),
        )
      case None => (robot.position, robot.orientation)
