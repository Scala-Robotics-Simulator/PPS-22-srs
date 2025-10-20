package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.action.SequenceAction.thenDo
import io.github.srs.model.entity.dynamicentity.action.{ Action, NoAction }
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.entity.dynamicentity.actuator.{ given_Kinematics_Agent, DifferentialWheelMotor, Wheel }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor, SensorReading }
import io.github.srs.model.environment.Environment
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AgentTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(3.0, 1.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val deltaTime: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val wheelMotor: DifferentialWheelMotor[Agent] =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(0.5)), Wheel(1.0, ShapeType.Circle(0.5)))

  val proximitySensor: Sensor[Agent, Environment] =
    ProximitySensor(Orientation(0.0), 3.0)

  val defaultAgent: Agent = agent at initialPosition withShape shape withOrientation initialOrientation

  val emptyActions: Action[IO] = NoAction[IO]()

  given CanEqual[Point2D, Point2D] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived

  "Agent" should "have an initial position" in:
    inside(defaultAgent.validate):
      case Right(a) => a.position.should(be(initialPosition))

  it should "support having sequence empty of actuators" in:
    inside((defaultAgent withActuators Seq.empty).validate):
      case Right(a) => a.actuators.should(be(Seq.empty))

  it should "support having one WheelMotor Actuator" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) => a.actuators.should(be(Seq(wheelMotor)))

  it should "stay at the same position if no movement occurs" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) => a.position.should(be(initialPosition))

  it should "return the same orientation if no movement occurs" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) => a.orientation.should(be(initialOrientation))

  it should "stay at the same position if it has no wheel motors" in:
    inside(defaultAgent.validate):
      case Right(a) =>
        a.position.should(be(initialPosition))

  it should "return the same orientation if it has no wheel motors" in:
    inside(defaultAgent.validate):
      case Right(a) =>
        a.orientation.should(be(initialOrientation))

  it should "keep moving with the same velocity if it has no actions" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val movedAgent = motor.applyMovementActions(a, deltaTime, emptyActions).unsafeRunSync()
        movedAgent.position._1.should(be > initialPosition._1)

  it should "return the same orientation if it has no actions" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val movedAgent = motor.applyMovementActions(a, deltaTime, emptyActions).unsafeRunSync()
        movedAgent.orientation.should(be(initialOrientation))

  it should "update its position based on a single MoveForward action" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val movedAgent = motor.applyMovementActions(a, deltaTime, moveForward[IO]).unsafeRunSync()
        movedAgent.position._1.should(be > initialPosition._1)

  it should "maintain orientation based on a single MoveForward action" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val movedAgent = motor.applyMovementActions(a, deltaTime, moveForward[IO]).unsafeRunSync()
        movedAgent.orientation.degrees.shouldBe(initialOrientation.degrees +- 0.1)

  it should "update its position based on a sequence of actions" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val moved1 = motor.applyMovementActions(a, deltaTime, moveForward[IO]).unsafeRunSync()
        val moved2 = motor.applyMovementActions(moved1, deltaTime, turnLeft[IO]).unsafeRunSync()
        val moved3 = motor.applyMovementActions(moved2, deltaTime, stop[IO]).unsafeRunSync()

        val expectedPosition = moved3.position
        val movements = moveForward[IO] thenDo turnLeft[IO] thenDo stop[IO]
        val movedAgent = motor.applyMovementActions(a, deltaTime, movements).unsafeRunSync()

        movedAgent.position.should(be(expectedPosition))

  it should "update its orientation based on a sequence of actions" in:
    inside((defaultAgent containing wheelMotor).validate):
      case Right(a) =>
        val motor = a.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }.value
        val moved1 = motor.applyMovementActions(a, deltaTime, moveForward[IO]).unsafeRunSync()
        val moved2 = motor.applyMovementActions(moved1, deltaTime, turnLeft[IO]).unsafeRunSync()
        val moved3 = motor.applyMovementActions(moved2, deltaTime, stop[IO]).unsafeRunSync()

        val expectedOrientation = moved3.orientation
        val movements = moveForward[IO] thenDo turnLeft[IO] thenDo stop[IO]
        val movedAgent = motor.applyMovementActions(a, deltaTime, movements).unsafeRunSync()

        movedAgent.orientation.degrees.should(be(expectedOrientation.degrees))

  it should "support sensors" in:
    inside((defaultAgent containing proximitySensor).validate):
      case Right(a) =>
        a.sensors.should(contain(proximitySensor))

  it should "sense the environment using its sensors" in:
    inside((defaultAgent containing proximitySensor).validate):
      case Right(a) =>
        val environment = Environment(10, 10)
        val sensedData = a.sensors.map(sensor =>
          SensorReading(sensor, sensor.sense[IO](a, environment).unsafeRunSync())
        )
        sensedData.should(
          contain.only(
            SensorReading(
              proximitySensor,
              proximitySensor.sense[IO](a, environment).unsafeRunSync(),
            ),
          ),
        )

end AgentTest
