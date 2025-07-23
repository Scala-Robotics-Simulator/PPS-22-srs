package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.WheelMotor.{ applyActions, move }
import io.github.srs.model.entity.dynamicentity.WheelMotorTestUtils.calculateMovement
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val wheelMotor: WheelMotor =
    WheelMotor(DeltaTime(0.1).toOption.value, Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  "Robot" should "have an initial position" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq.empty)):
      case Right(robot) => robot.position should be(initialPosition)

  it should "support having sequence empty of actuators" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq.empty)):
      case Right(robot) => robot.actuators should be(Seq.empty)

  it should "support having one WheelMotor Actuator" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) => robot.actuators should be(Seq(wheelMotor))

  it should "stay at the same position if no movement occurs" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) => robot.position should be(initialPosition)

  it should "return the same orientation if no movement occurs" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) => robot.orientation should be(initialOrientation)

  it should "stay at the same position if it has no wheel motors" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq.empty)):
      case Right(robot) =>
        val movedRobot: Robot = robot.move
        movedRobot.position should be(initialPosition)

  it should "return the same orientation if it has no wheel motors" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq.empty)):
      case Right(robot) =>
        val movedRobot: Robot = robot.move
        movedRobot.orientation should be(initialOrientation)

  it should "stay at the same position if it has no actions" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyActions(Seq.empty)
        movedRobot.position should be(initialPosition)

  it should "return the same orientation if it has no actions" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyActions(Seq.empty)
        movedRobot.orientation should be(initialOrientation)

  it should "update its position based on a single MoveForward action" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val movedRobot = robot.applyActions(Seq(Action.MoveForward))
        val expectedMovement: (Point2D, Orientation) = calculateMovement(robot)
        movedRobot.position shouldBe expectedMovement._1

  it should "update its orientation based on a single MoveForward action" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val movedRobot = robot.applyActions(Seq(Action.MoveForward))
        val expectedMovement: (Point2D, Orientation) = calculateMovement(robot)
        movedRobot.orientation.degrees shouldBe expectedMovement._2.degrees

  it should "update its position based on a sequence of actions" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val moved1 = robot.applyActions(Seq(Action.MoveForward))
        val moved2 = moved1.applyActions(Seq(Action.TurnLeft))
        val moved3 = moved2.applyActions(Seq(Action.Stop))

        val expectedPosition = moved3.position
        val movedRobot = robot.applyActions(Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
        movedRobot.position shouldBe expectedPosition

  it should "update its orientation based on a sequence of actions" in:
    inside(Robot(initialPosition, shape, initialOrientation, Seq(wheelMotor))):
      case Right(robot) =>
        val moved1 = robot.applyActions(Seq(Action.MoveForward))
        val moved2 = moved1.applyActions(Seq(Action.TurnLeft))
        val moved3 = moved2.applyActions(Seq(Action.Stop))

        val expectedOrientation = moved3.orientation
        val movedRobot = robot.applyActions(Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
        movedRobot.orientation.degrees shouldBe expectedOrientation.degrees

end RobotTest
