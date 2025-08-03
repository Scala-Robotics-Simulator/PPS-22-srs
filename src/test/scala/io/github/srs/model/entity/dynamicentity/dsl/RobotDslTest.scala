package io.github.srs.model.entity.dynamicentity.dsl

import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorSuite }
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[ShapeType.Circle, ShapeType.Circle] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  given CanEqual[Actuator[Robot], Actuator[Robot]] = CanEqual.derived
  given CanEqual[SensorSuite, SensorSuite] = CanEqual.derived

  import RobotDsl.*

  val dt: DeltaTime = DeltaTime(0.1).toOption.value
  val wheelRadius: Double = 0.5

  val wheelMotor: WheelMotor =
    WheelMotor(dt, Wheel(1.0, ShapeType.Circle(wheelRadius)), Wheel(2.0, ShapeType.Circle(wheelRadius)))

  val wheelMotor2: WheelMotor =
    WheelMotor(dt, Wheel(3.0, ShapeType.Circle(wheelRadius)), Wheel(4.0, ShapeType.Circle(wheelRadius)))

  "Robot DSL" should "create a robot with default properties" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*
    val entity = robot
    val _ = entity.position shouldBe defaultPosition
    val _ = entity.shape shouldBe defaultShape
    val _ = entity.orientation shouldBe defaultOrientation
    val _ = entity.actuators shouldBe defaultActuators
    entity.sensors shouldBe defaultSensorSuite

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
    val sensor = ProximitySensor(Orientation(0.0), 0.5, 3.0).toOption.value
    val sensors = SensorSuite(sensor)
    val entity = robot withSensors sensors
    entity.sensors shouldBe sensors

  it should "add the actuator using convenience method" in:
    val entity = robot containing wheelMotor
    entity.actuators should contain(wheelMotor)

  it should "add multiple actuators using convenience methods" in:
    val entity = robot containing wheelMotor and wheelMotor2
    entity.actuators should contain allOf (wheelMotor, wheelMotor2)

  it should "validate the robot with default properties" in:
    val entity = robot
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "validate the robot with custom properties" in:
    val entity = robot at Point2D(1.0, 2.0) withShape ShapeType.Circle(0.5) withOrientation Orientation(
      90.0,
    ) containing wheelMotor withSensors SensorSuite.empty
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with invalid properties" in:
    val entity = robot containing wheelMotor and wheelMotor2
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true
end RobotDslTest
