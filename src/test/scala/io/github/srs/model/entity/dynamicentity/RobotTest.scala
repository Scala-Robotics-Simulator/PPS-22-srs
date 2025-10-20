package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.action.SequenceAction.thenDo
import io.github.srs.model.entity.dynamicentity.action.{ Action, NoAction }
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotorTestUtils.calculateMovement
import io.github.srs.model.entity.dynamicentity.actuator.{ given_Kinematics_Robot, DifferentialWheelMotor, Wheel }
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor, SensorReading }
import io.github.srs.model.environment.Environment
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.*
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

  val emptyActions: Action[IO] = NoAction[IO]()

  given CanEqual[Point2D, Point2D] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived

  "Robot" should "have an initial position" in:
    inside(defaultRobot.validate):
      case Right(robot) => robot.position.should(be(initialPosition))

  it should "support having sequence empty of actuators" in:
    inside((defaultRobot withActuators Seq.empty).validate):
      case Right(robot) => robot.actuators.should(be(Seq.empty))

  it should "support having one WheelMotor Actuator" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.actuators.should(be(Seq(wheelMotor)))

  it should "stay at the same position if no movement occurs" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.position.should(be(initialPosition))

  it should "return the same orientation if no movement occurs" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) => robot.orientation.should(be(initialOrientation))

  it should "stay at the same position if it has no wheel motors" in:
    inside(defaultRobot.validate):
      case Right(robot) =>
        robot.position.should(be(initialPosition))

  it should "return the same orientation if it has no wheel motors" in:
    inside(defaultRobot.validate):
      case Right(robot) =>
        robot.orientation.should(be(initialOrientation))

  it should "keep moving with the same velocity if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val movedRobot = motor.applyMovementActions(robot, deltaTime, emptyActions).unsafeRunSync()
        movedRobot.position._1.should(be > initialPosition._1)

  it should "return the same orientation if it has no actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val movedRobot = motor.applyMovementActions(robot, deltaTime, emptyActions).unsafeRunSync()
        movedRobot.orientation.should(be(initialOrientation))

  it should "update its position based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val movedRobot = motor.applyMovementActions(robot, deltaTime, moveForward[IO]).unsafeRunSync()
        val expectedMovement = calculateMovement(deltaTime, robot)
        movedRobot.position.should(be(expectedMovement._1))

  it should "update its orientation based on a single MoveForward action" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val movedRobot = motor.applyMovementActions(robot, deltaTime, moveForward[IO]).unsafeRunSync()
        val expectedMovement = calculateMovement(deltaTime, robot)
        movedRobot.orientation.degrees.should(be(expectedMovement._2.degrees))

  it should "update its position based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val moved1 = motor.applyMovementActions(robot, deltaTime, moveForward[IO]).unsafeRunSync()
        val moved2 = motor.applyMovementActions(moved1, deltaTime, turnLeft[IO]).unsafeRunSync()
        val moved3 = motor.applyMovementActions(moved2, deltaTime, stop[IO]).unsafeRunSync()

        val expectedPosition = moved3.position
        val movements = moveForward[IO] thenDo turnLeft[IO] thenDo stop[IO]
        val movedRobot = motor.applyMovementActions(robot, deltaTime, movements).unsafeRunSync()

        movedRobot.position.should(be(expectedPosition))

  it should "update its orientation based on a sequence of actions" in:
    inside((defaultRobot containing wheelMotor).validate):
      case Right(robot) =>
        val motor = robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }.value
        val moved1 = motor.applyMovementActions(robot, deltaTime, moveForward[IO]).unsafeRunSync()
        val moved2 = motor.applyMovementActions(moved1, deltaTime, turnLeft[IO]).unsafeRunSync()
        val moved3 = motor.applyMovementActions(moved2, deltaTime, stop[IO]).unsafeRunSync()

        val expectedOrientation = moved3.orientation
        val movements = moveForward[IO] thenDo turnLeft[IO] thenDo stop[IO]
        val movedRobot = motor.applyMovementActions(robot, deltaTime, movements).unsafeRunSync()

        movedRobot.orientation.degrees.should(be(expectedOrientation.degrees))

  it should "support sensors" in:
    inside((defaultRobot containing proximitySensor).validate):
      case Right(robot) =>
        robot.sensors.should(contain(proximitySensor))

  it should "sense the environment using its sensors" in:
    import Sensor.senseAll
    inside((defaultRobot containing proximitySensor).validate):
      case Right(robot) =>
        val environment = Environment(10, 10)
        val sensedData = robot.senseAll[IO](environment).unsafeRunSync()
        sensedData.should(
          contain.only(
            SensorReading(
              proximitySensor,
              proximitySensor.sense[IO](robot, environment).unsafeRunSync(),
            ),
          ),
        )

end RobotTest
