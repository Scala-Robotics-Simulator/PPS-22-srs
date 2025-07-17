package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.dynamicentity.WheelMotor.move
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WheelMotorTest extends AnyFlatSpec with Matchers:

  val dt: Double = 0.1
  val wheelRadius: Double = 0.5
  val shape: ShapeType.Circle = ShapeType.Circle(1.0)
  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)

  "WheelMotor" should "update its position based on the wheel speeds" in:
    val leftSpeed: Double = 1.0
    val rightSpeed: Double = 2.0

    val vLeft: Double = leftSpeed * wheelRadius
    val vRight: Double = rightSpeed * wheelRadius
    val velocity: Double = (vLeft + vRight) / 2
    val theta: Double = initialOrientation.toRadians
    val dx: Double = velocity * math.cos(theta) * dt
    val dy: Double = velocity * math.sin(theta) * dt
    val expectedPosition: Point2D = Point2D(initialPosition.x + dx, initialPosition.y + dy)

    val wheelMotor: WheelMotor = WheelMotor(
      dt,
      Wheel(leftSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightSpeed, ShapeType.Circle(wheelRadius)),
    )

    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot: Robot = robot.move

    movedRobot.position shouldBe expectedPosition

  it should "update its orientation based on the wheel speeds" in:
    val leftSpeed: Double = 1.0
    val rightSpeed: Double = 2.0

    val vLeft: Double = leftSpeed * wheelRadius
    val vRight: Double = rightSpeed * wheelRadius
    val wheelDistance: Double = shape.radius * 2
    val omega: Double = (vRight - vLeft) / wheelDistance
    val expectedOrientation: Orientation = Orientation.fromRadians(initialOrientation.toRadians + omega * dt)

    val wheelMotor: WheelMotor = WheelMotor(
      dt,
      Wheel(leftSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightSpeed, ShapeType.Circle(wheelRadius)),
    )

    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot: Robot = robot.move

    movedRobot.orientation.degrees shouldBe expectedOrientation.degrees
end WheelMotorTest
