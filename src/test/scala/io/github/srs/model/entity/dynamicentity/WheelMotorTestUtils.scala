package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.entity.*

object WheelMotorTestUtils:

  def calculateMovement(robot: Robot): (Point2D, Orientation) =
    robot.actuators.collectFirst { case wm: WheelMotor => wm } match
      case Some(wm) =>
        val vLeft = wm.left.speed * wm.left.shape.radius
        val vRight = wm.right.speed * wm.right.shape.radius
        val velocity = (vLeft + vRight) / 2
        val theta = robot.orientation.toRadians
        val wheelDistance = robot.shape.radius * 2
        val omega = (vRight - vLeft) / wheelDistance
        val dx = velocity * math.cos(theta) * wm.dt
        val dy = velocity * math.sin(theta) * wm.dt
        (Point2D(robot.position.x + dx, robot.position.y + dy), Orientation.fromRadians(theta + omega * wm.dt))
      case None => (robot.position, robot.orientation)
