package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.WheelMotor.move
import io.github.srs.model.entity.dynamicentity.WheelMotor.applyActions
import io.github.srs.model.entity.dynamicentity.WheelMotorTestUtils.calculateMovement

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

  it should "stay at the same position if it has no actions" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot: Robot = robot.applyActions(Seq.empty)
    movedRobot.position should be(Point2D(0.0, 0.0))

  it should "return the same orientation if it has no actions" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot: Robot = robot.applyActions(Seq.empty)
    movedRobot.orientation should be(Orientation(0.0))

  it should "update its position based on a single MoveForward action" in:
    val robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot = robot.applyActions(Seq(Action.MoveForward))
    val expectedMovement: (Point2D, Orientation) = calculateMovement(robot)
    movedRobot.position shouldBe expectedMovement._1

  it should "update its orientation based on a single MoveForward action" in:
    val robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val movedRobot = robot.applyActions(Seq(Action.MoveForward))
    val expectedMovement: (Point2D, Orientation) = calculateMovement(robot)
    movedRobot.orientation.degrees shouldBe expectedMovement._2.degrees

  it should "update its position based on a sequence of actions" in:
    val robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))

    val moved1 = robot.applyActions(Seq(Action.MoveForward))
    val moved2 = moved1.applyActions(Seq(Action.TurnLeft))
    val moved3 = moved2.applyActions(Seq(Action.Stop))

    val expectedPosition = moved3.position
    val movedRobot = robot.applyActions(Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
    movedRobot.position shouldBe expectedPosition

  it should "update its orientation based on a sequence of actions" in:
    val robot: Robot = Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))
    val moved1 = robot.applyActions(Seq(Action.MoveForward))
    val moved2 = moved1.applyActions(Seq(Action.TurnLeft))
    val moved3 = moved2.applyActions(Seq(Action.Stop))

    val expectedOrientation = moved3.orientation
    val movedRobot = robot.applyActions(Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
    movedRobot.orientation.degrees shouldBe expectedOrientation.degrees

end RobotTest
