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
      val vLeft = this.left.speed * this.left.shape.radius
      val vRight = this.right.speed * this.right.shape.radius
      val wheelDistance = robot.shape.radius * 2
      val velocity = (vLeft + vRight) / 2
      val omega = (vRight - vLeft) / wheelDistance
      val theta = robot.orientation.toRadians
      val dx = velocity * math.cos(theta) * dt
      val dy = velocity * math.sin(theta) * dt
      val newPosition = Point2D(robot.position.x + dx, robot.position.y + dy)
      val newOrientation = Orientation.fromRadians(theta + omega * dt)
      Robot(newPosition, robot.shape, newOrientation, robot.actuators)

  end DifferentialWheelMotor
end WheelMotor
