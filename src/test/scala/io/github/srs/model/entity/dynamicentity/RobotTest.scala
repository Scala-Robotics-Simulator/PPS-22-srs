package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.WheelMotor.move

class RobotTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
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

end RobotTest
