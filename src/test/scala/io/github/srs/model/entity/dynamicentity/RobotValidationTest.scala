package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

class RobotValidationTest extends AnyFlatSpec with Matchers:

  val wheelMotor: DifferentialWheelMotor =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  it should "not support having multiple WheelMotor Actuators" in:
    inside((robot containing wheelMotor and wheelMotor).validate):
      case Left(DomainError.InvalidCount("actuators", 2, 0, 1)) => succeed

  it should "not support having NaN position" in:
    inside((robot at Point2D(Double.NaN, 0.0)).validate):
      case Left(DomainError.NotANumber("x", value)) => assert(value.isNaN)

  it should "not support having Infinite position" in:
    inside((robot at Point2D(0.0, Double.PositiveInfinity)).validate):
      case Left(DomainError.Infinite("y", value)) => assert(value.isInfinite)

  it should "not support having NaN orientation" in:
    inside((robot withOrientation Orientation(Double.NaN)).validate):
      case Left(DomainError.NotANumber("degrees", value)) => assert(value.isNaN)
end RobotValidationTest
