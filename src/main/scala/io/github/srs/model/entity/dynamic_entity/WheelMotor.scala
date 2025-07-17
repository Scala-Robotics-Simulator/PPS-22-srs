package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.*

trait WheelMotor extends Actuator:
  def dt: Double
  def left: Wheel
  def right: Wheel

object WheelMotor:

  def apply(dt: Double, left: Wheel, right: Wheel): WheelMotor =
    new DifferentialWheelMotor(dt, left, right)

  private case class DifferentialWheelMotor(val dt: Double, val left: Wheel, val right: Wheel) extends WheelMotor:

    override def act(robot: Robot): Robot = move(robot)

    def move(robot: Robot): Robot =
      robot.actuators match
        case Seq(wm: WheelMotor) =>
          val leftSpeed = wm.left.speed
          val rightSpeed = wm.right.speed
          val radius = robot.shape.radius
          val wheelDistance = radius * 2
          val speed = (leftSpeed + rightSpeed) / 2
          val omega = (rightSpeed - leftSpeed) / wheelDistance
          val newPosition = Point2D(
            robot.position.x + speed * Math.cos(robot.orientation.toRadians) * dt,
            robot.position.y + speed * Math.sin(robot.orientation.toRadians) * dt,
          )
          val newOrientation = Orientation(robot.orientation.toRadians + omega * dt)
          Robot(newPosition, robot.shape, newOrientation, robot.actuators)
        case _ => robot
  end DifferentialWheelMotor
end WheelMotor
