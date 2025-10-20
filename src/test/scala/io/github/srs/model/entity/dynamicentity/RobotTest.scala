package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.Id
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.action.SequenceAction.thenDo
import io.github.srs.model.entity.dynamicentity.action.{ Action, ActionAlgebra, NoAction }
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.{ applyMovementActions, move }
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotorTestUtils.calculateMovement
import io.github.srs.model.entity.dynamicentity.actuator.{ DifferentialWheelMotor, Wheel }
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor, SensorReading }
import io.github.srs.model.environment.Environment
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(3.0, 1.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val deltaTime: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val wheelMotor: DifferentialWheelMotor[Robot] =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  val proximitySensor: Sensor[Robot, Environment] =
    ProximitySensor(Orientation(0.0), 3.0)

  val defaultRobot: Robot = robot at initialPosition withShape shape withOrientation initialOrientation

  val emptyActions: Action[Id] = NoAction[Id]()

  given CanEqual[Point2D, Point2D] = CanEqual.derived

  given CanEqual[Orientation, Orientation] = CanEqual.derived

  given actionAlgebra: ActionAlgebra[Id, Robot] with

    def moveWheels(robot: Robot, left: Double, right: Double): Robot =
      robot.copy(
        actuators = Seq(
          DifferentialWheelMotor(
            Wheel(left, ShapeType.Circle(0.5)),
            Wheel(right, ShapeType.Circle(0.5)),
          ),
        ),
      )

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

  it should "keep moving with the same velocity if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyMovementActions[Id](deltaTime, emptyActions)
        movedRobot.position._1 should be > initialPosition._1

  it should "return the same orientation if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot: Robot = robot.applyMovementActions[Id](deltaTime, emptyActions)
        movedRobot.orientation should be(initialOrientation)

  it should "update its position based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot = robot.applyMovementActions[Id](deltaTime, moveForward)
        val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
        movedRobot.position should be(expectedMovement._1)

  it should "update its orientation based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val movedRobot = robot.applyMovementActions[Id](deltaTime, moveForward)
        val expectedMovement: (Point2D, Orientation) = calculateMovement(deltaTime, robot)
        movedRobot.orientation.degrees should be(expectedMovement._2.degrees)

  it should "update its position based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val moved1 = robot.applyMovementActions[Id](deltaTime, moveForward[Id])
        val moved2 = moved1.applyMovementActions[Id](deltaTime, turnLeft[Id])
        val moved3 = moved2.applyMovementActions[Id](deltaTime, stop[Id])

        val expectedPosition = moved3.position
        val movements: Action[Id] = moveForward[Id] thenDo turnLeft[Id] thenDo stop[Id]
        val movedRobot: Id[Robot] = robot.applyMovementActions[Id](deltaTime, movements)
        movedRobot.position should be(expectedPosition)

  it should "update its orientation based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val moved1 = robot.applyMovementActions[Id](deltaTime, moveForward[Id])
        val moved2 = moved1.applyMovementActions[Id](deltaTime, turnLeft[Id])
        val moved3 = moved2.applyMovementActions[Id](deltaTime, stop[Id])

        val expectedOrientation = moved3.orientation
        val movements: Action[Id] = moveForward[Id] thenDo turnLeft[Id] thenDo stop[Id]
        val movedRobot: Id[Robot] = robot.applyMovementActions[Id](deltaTime, movements)
        movedRobot.orientation.degrees should be(expectedOrientation.degrees)

  it should "move correctly with custom actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val move1: Action[Id] = customMove[Id](0.5, 0.5).toOption.value
        val move2: Action[Id] = customMove[Id](0.5, 0.5).toOption.value
        val movedRobot = robot.applyMovementActions[Id](deltaTime, move1 thenDo move2)
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
        sensedData should contain only SensorReading(
          proximitySensor,
          proximitySensor.sense[Id](robot, environment),
        )

end RobotTest
