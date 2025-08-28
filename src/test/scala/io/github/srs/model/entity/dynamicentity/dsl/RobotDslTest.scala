package io.github.srs.model.entity.dynamicentity.dsl

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor, Wheel }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.behavior.{ BehaviorContext, Policy }
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor }
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[ShapeType.Circle, ShapeType.Circle] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  given CanEqual[Actuator[Robot], Actuator[Robot]] = CanEqual.derived
  given CanEqual[Sensor[Robot, Environment], Sensor[Robot, Environment]] = CanEqual.derived
  given CanEqual[Behavior[BehaviorContext, Action[IO]], Behavior[BehaviorContext, Action[IO]]] = CanEqual.derived
  given CanEqual[Policy, Policy] = CanEqual.derived

  import RobotDsl.*

  val dt: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val wheelRadius: Double = 0.5

  val wheelMotor: DifferentialWheelMotor =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(wheelRadius)), Wheel(1.0, ShapeType.Circle(wheelRadius)))

  val wheelMotor2: DifferentialWheelMotor =
    DifferentialWheelMotor(Wheel(0.5, ShapeType.Circle(wheelRadius)), Wheel(0.2, ShapeType.Circle(wheelRadius)))

  val sensor: Sensor[Robot, Environment] = ProximitySensor(Orientation(0.0), 3.0)

  val sensor2: Sensor[Robot, Environment] = ProximitySensor(Orientation(90.0), 3.0)

  "Robot DSL" should "create a robot with default properties" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*
    val entity = robot
    val _ = entity.position shouldBe DefaultPosition
    val _ = entity.shape shouldBe DefaultShape
    val _ = entity.orientation shouldBe DefaultOrientation
    val _ = entity.actuators shouldBe DefaultActuators
    entity.sensors shouldBe DefaultSensors

  it should "set the position of the robot" in:
    val pos = Point2D(5.0, 5.0)
    val entity = robot at pos
    entity.position shouldBe pos

  it should "set the shape of the robot" in:
    val shape: ShapeType.Circle = ShapeType.Circle(1.0)
    val entity = robot withShape shape
    entity.shape shouldBe shape

  it should "set the orientation of the robot" in:
    val orientation = Orientation(45.0)
    val entity = robot withOrientation orientation
    entity.orientation shouldBe orientation

  it should "set the actuators of the robot" in:
    val actuators = Seq(wheelMotor)
    val entity = robot withActuators actuators
    entity.actuators shouldBe actuators

  it should "set the sensors of the robot" in:
    val sensors = Seq(sensor)
    val entity = robot withSensors sensors
    entity.sensors shouldBe sensors

  it should "add the actuator using convenience method" in:
    val entity = robot containing wheelMotor
    entity.actuators should contain(wheelMotor)

  it should "add multiple actuators using convenience methods" in:
    val entity = robot containing wheelMotor and wheelMotor2
    entity.actuators should contain allOf (wheelMotor, wheelMotor2)

  it should "add the sensor using convenience method" in:
    val entity = robot containing sensor
    entity.sensors should contain(sensor)

  it should "add multiple sensors using convenience methods" in:
    val entity = robot containing sensor and sensor2
    entity.sensors should contain allOf (sensor, sensor2)

  it should "validate the robot with default properties" in:
    val entity = robot
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "validate the robot with custom properties" in:
    val entity = robot at Point2D(1.0, 2.0) withShape ShapeType.Circle(0.5) withOrientation Orientation(
      90.0,
    ) containing wheelMotor withSensors Seq.empty
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with invalid properties" in:
    val entity = robot containing wheelMotor and wheelMotor2
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "set the behavior of the robot" in:
    val behavior = Policy.AlwaysForward
    val entity = robot withBehavior behavior
    entity.behavior shouldBe behavior
end RobotDslTest
