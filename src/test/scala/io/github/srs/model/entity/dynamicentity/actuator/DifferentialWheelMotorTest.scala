package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.Id
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.move
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotorTestUtils.calculateMovement
import io.github.srs.model.entity.dynamicentity.actuator.Wheel
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

class DifferentialWheelMotorTest extends AnyFlatSpec with Matchers:

  val deltaTime: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val wheelRadius: Double = 0.5
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)
  val initialPosition: Point2D = Point2D(3.0, 1.0)
  val initialOrientation: Orientation = Orientation(0.0)

  "WheelMotor" should "update its position based on the wheel speeds" in:
    val leftSpeed: Double = 1.0
    val rightSpeed: Double = 1.0
    val wheelMotor: DifferentialWheelMotor = DifferentialWheelMotor(
      Wheel(leftSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightSpeed, ShapeType.Circle(wheelRadius)),
    )
    val robot: Robot =
      Robot(
        position = initialPosition,
        shape = shape,
        orientation = initialOrientation,
        actuators = Seq(wheelMotor),
      ).validate.toOption.value
    val movedRobot: Robot = robot.move[Id](deltaTime)

    val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
    movedRobot.position shouldBe expectedMovement._1

  it should "update its orientation based on the wheel speeds" in:
    val leftSpeed: Double = 0.5
    val rightSpeed: Double = 1.0
    val wheelMotor: DifferentialWheelMotor = DifferentialWheelMotor(
      Wheel(leftSpeed, ShapeType.Circle(wheelRadius)),
      Wheel(rightSpeed, ShapeType.Circle(wheelRadius)),
    )
    val robot: Robot =
      Robot(
        position = initialPosition,
        shape = shape,
        orientation = initialOrientation,
        actuators = Seq(wheelMotor),
      ).validate.toOption.value
    val movedRobot: Robot = robot.move[Id](deltaTime)

    val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
    movedRobot.orientation.degrees shouldBe expectedMovement._2.degrees
end DifferentialWheelMotorTest
