package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.dynamic_entity.Robot
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotTest extends AnyFlatSpec with Matchers:

  val p: Point2D = Point2D(0.0, 0.0)
  val s: ShapeType.Circle = ShapeType.Circle(1.0)
  val o: Orientation = Orientation(0.0)
  val wheelMotor = WheelMotor(0.1, Wheel(1.0, ShapeType.Circle(0.05)), Wheel(1.0, ShapeType.Circle(0.05)))

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
    val movedRobot: Robot = wheelMotor.act(robot)
    movedRobot.position should be(Point2D(0.1, 0.0))
end RobotTest
