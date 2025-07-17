package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*

trait WheelMotor extends Actuator[Robot]:
  def dt: Double
  def left: Wheel
  def right: Wheel

object WheelMotor:

  extension (robot: Robot)

    def move: Robot =
      robot.actuators.collectFirst { case wm: WheelMotor => wm } match
        case Some(wm) => wm.act(robot)
        case None => robot

  def apply(dt: Double, left: Wheel, right: Wheel): WheelMotor =
    new DifferentialWheelMotor(dt, left, right)

  private case class DifferentialWheelMotor(val dt: Double, val left: Wheel, val right: Wheel) extends WheelMotor:

    override def act(robot: Robot): Robot =
      val leftSpeed = this.left.speed * this.left.shape.radius
      val rightSpeed = this.right.speed * this.right.shape.radius
      val radius = robot.shape.radius
      val wheelDistance = radius * 2
      val speed = (leftSpeed + rightSpeed) / 2
      val omega = (rightSpeed - leftSpeed) / wheelDistance
      val newPosition = Point2D(
        robot.position.x + speed * math.cos(robot.orientation.toRadians) * dt,
        robot.position.y + speed * math.sin(robot.orientation.toRadians) * dt,
      )
      val newOrientation = Orientation(robot.orientation.toRadians + omega * dt)
      Robot(newPosition, robot.shape, newOrientation, robot.actuators)

  end DifferentialWheelMotor
end WheelMotor
