package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotValidationTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val wheelMotor: WheelMotor =
    WheelMotor(DeltaTime(0.1).toOption.value, Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  it should "not support having multiple WheelMotor Actuators" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor, wheelMotor))):
      case Left(DomainError.InvalidCount("actuators", 2, 0, 1)) => succeed

  it should "not support having NaN position" in:
    inside(Robot(Point2D(Double.NaN, 0.0), shape, initialOrientation, Seq(wheelMotor))):
      case Left(DomainError.NotANumber("x", value)) => assert(value.isNaN)

  it should "not support having Infinite position" in:
    inside(Robot(Point2D(0.0, Double.PositiveInfinity), shape, initialOrientation, Seq(wheelMotor))):
      case Left(DomainError.Infinite("y", value)) => assert(value.isInfinite)

  it should "not support having NaN orientation" in:
    inside(Robot(initialPosition, shape, Orientation(Double.NaN), Seq(wheelMotor))):
      case Left(DomainError.NotANumber("degrees", value)) => assert(value.isNaN)

  it should "not support having Infinite orientation" in:
    inside(Robot(initialPosition, shape, Orientation(Double.NegativeInfinity), Seq(wheelMotor))):
      case Left(DomainError.NotANumber("degrees", value)) => assert(value.isNaN)
end RobotValidationTest
