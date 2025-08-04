package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.Id
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.WheelMotorTestUtils.calculateMovement
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor, SensorReading }
import io.github.srs.model.environment.Environment
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.DifferentialWheelMotor.move
import io.github.srs.model.entity.dynamicentity.DifferentialWheelMotor.applyActions

class RobotTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val deltaTime: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val wheelMotor: DifferentialWheelMotor =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  val proximitySensor: Sensor[Robot, Environment] =
    ProximitySensor(Orientation(0.0), 0.5, 3.0)

  val defaultRobot: Robot = robot at initialPosition withShape shape withOrientation initialOrientation

  "Robot" should "have an initial position" in:
    inside(defaultRobot.validate):
      case Right(robot) => robot.position should be(initialPosition)

  it should "support having sequence empty of actuators" in:
    inside((defaultRobot withActuators Seq.empty).validate):
      case Right(robot) => robot.actuators should be(Seq.empty)

  it should "support having one WheelMotor Actuator" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.actuators should be(Seq(wheelMotor))

  it should "stay at the same position if no movement occurs" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.position should be(initialPosition)

  it should "return the same orientation if no movement occurs" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.orientation should be(initialOrientation)

  it should "stay at the same position if it has no wheel motors" in:
    inside(defaultRobot.validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.move[Id](deltaTime)
        movedRobot.position should be(initialPosition)

  it should "return the same orientation if it has no wheel motors" in:
    inside(defaultRobot.validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.move[Id](deltaTime)
        movedRobot.orientation should be(initialOrientation)

  it should "stay at the same position if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyActions[Id](deltaTime, Seq.empty)
        movedRobot.position should be(initialPosition)

  it should "return the same orientation if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyActions[Id](deltaTime, Seq.empty)
        movedRobot.orientation should be(initialOrientation)

  it should "update its position based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward))
        val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
        movedRobot.position should be(expectedMovement._1)

  it should "update its orientation based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward))
        val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
        movedRobot.orientation.degrees should be(expectedMovement._2.degrees)

  it should "update its position based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val moved1 = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward))
        val moved2 = moved1.applyActions[Id](deltaTime, Seq(Action.TurnLeft))
        val moved3 = moved2.applyActions[Id](deltaTime, Seq(Action.Stop))

        val expectedPosition = moved3.position
        val movedRobot = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
        movedRobot.position should be(expectedPosition)

  it should "update its orientation based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val moved1 = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward))
        val moved2 = moved1.applyActions[Id](deltaTime, Seq(Action.TurnLeft))
        val moved3 = moved2.applyActions[Id](deltaTime, Seq(Action.Stop))

        val expectedOrientation = moved3.orientation
        val movedRobot = robot.applyActions[Id](deltaTime, Seq(Action.MoveForward, Action.TurnLeft, Action.Stop))
        movedRobot.orientation.degrees should be(expectedOrientation.degrees)

  it should "move correctly with custom actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val customAction = Action.move(0.5, 0.5).toOption.value
        val movedRobot = robot.applyActions[Id](deltaTime, Seq(customAction, customAction))
        val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
        movedRobot.position should be(expectedMovement._1)

  it should "support sensors" in:
    inside((defaultRobot containing proximitySensor).validate):
      case Right(robot) =>
        robot.sensors should contain(proximitySensor)

  it should "sense the environment using its sensors" in:
    import Sensor.senseAll
    inside((defaultRobot containing proximitySensor).validate):
      case Right(robot) =>
        val environment = Environment(10, 10)
        val sensedData = robot.senseAll[Id](environment)
        sensedData should contain only SensorReading(proximitySensor, proximitySensor.sense[Id](robot, environment))

end RobotTest
