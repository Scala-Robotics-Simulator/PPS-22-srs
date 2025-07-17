package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.WheelMotor.move

class RobotTest extends AnyFlatSpec with Matchers:

  val p: Point2D = Point2D(0.0, 0.0)
  val s: ShapeType.Circle = ShapeType.Circle(1.0)
  val o: Orientation = Orientation(0.0)
  val wheelMotor = WheelMotor(0.1, Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  "Robot" should "have an initial position" in:
    val robot: Robot = Robot(p, s, o, Seq.empty)
    robot.position should be(Point2D(0.0, 0.0))

  it should "support having no actuators" in:
    val robot: Robot = Robot(p, s, o, Seq.empty)
    robot.actuators should be(Seq.empty)

  it should "support having some of WheelMotor Actuator" in:
    val robot: Robot = Robot(p, s, o, Seq(wheelMotor))
    robot.actuators should be(Seq(wheelMotor))

  it should "move to a new position" in:
    val robot: Robot = Robot(p, s, o, Seq(wheelMotor))
    val movedRobot: Robot = robot.move
    movedRobot.position should be(Point2D(0.05, 0.0))

  it should "stay at the same position if no movement occurs" in:
    val robot: Robot = Robot(p, s, o, Seq(wheelMotor))
    robot.position should be(Point2D(0.0, 0.0))

  it should "stay at the same position if it has no wheel motors" in:
    val robot: Robot = Robot(p, s, o, Seq.empty)
    val movedRobot: Robot = robot.move
    movedRobot.position should be(Point2D(0.0, 0.0))

  it should "update its orientation based on the wheel motors" in:
    val robot: Robot = Robot(p, s, Orientation(10.0), Seq(wheelMotor))
    val movedRobot: Robot = robot.move
    movedRobot.orientation.degrees should be(10.0)

end RobotTest
