package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.WheelMotor.move

class RobotTest extends AnyFlatSpec with Matchers:

  val initialPosition = Point2D(0.0, 0.0)
  val initialOrientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)
  val wheelMotor = WheelMotor(0.1, Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  "Robot" should "have an initial position" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq.empty)
    robot.position should be(Point2D(0.0, 0.0))

  it should "support having no actuators" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq.empty)
    robot.actuators should be(Seq.empty)

  it should "support having some of WheelMotor Actuator" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    robot.actuators should be(Seq(wheelMotor))

  it should "stay at the same position if no movement occurs" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    robot.position should be(Point2D(0.0, 0.0))

  it should "return the same orientation if no movement occurs" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    robot.orientation should be(initialOrientation)

  it should "stay at the same position if it has no wheel motors" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq.empty)
    val movedRobot: Robot = robot.move
    movedRobot.position should be(Point2D(0.0, 0.0))

  it should "return the same orientation if it has no wheel motors" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq.empty)
    val movedRobot: Robot = robot.move
    movedRobot.orientation should be(Orientation(0.0))

  it should "update its position based on the wheel motors" in:
    val dt = 0.1
    val leftWheelSpeed = 1.0
    val rightWheelSpeed = 2.0
    val wheelRadius = 0.5

    val vLeft = leftWheelSpeed * wheelRadius
    val vRight = rightWheelSpeed * wheelRadius
    val velocity = (vLeft + vRight) / 2
    val theta = initialOrientation.toRadians
    val dx = velocity * math.cos(theta) * dt
    val dy = velocity * math.sin(theta) * dt
    val expectedPosition = Point2D(initialPosition.x + dx, initialPosition.y + dy)

    val wheelMotor2 = WheelMotor(
      dt,
      Wheel(leftWheelSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightWheelSpeed, ShapeType.Circle(wheelRadius)),
    )
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor2))
    val movedRobot: Robot = robot.move

    movedRobot.position should be(expectedPosition)

  it should "update its orientation based on the wheel motors" in:
    val dt = 0.1
    val leftWheelSpeed = 1.0
    val wheelRadius = 0.5
    val rightWheelSpeed = 2.0

    val vLeft = leftWheelSpeed * wheelRadius
    val rLeft = rightWheelSpeed * wheelRadius
    val wheelDistance = shape.radius * 2
    val omega = (rLeft - vLeft) / wheelDistance
    val expectedOrientation = Orientation.fromRadians(initialOrientation.toRadians + omega * dt)

    val wheelMotor2 = WheelMotor(
      dt,
      Wheel(leftWheelSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightWheelSpeed, ShapeType.Circle(wheelRadius)),
    )
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor2))
    val movedRobot: Robot = robot.move

    movedRobot.orientation.degrees should be(expectedOrientation.degrees)

end RobotTest
