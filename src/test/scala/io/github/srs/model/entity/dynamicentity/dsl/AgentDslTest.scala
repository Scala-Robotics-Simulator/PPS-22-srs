package io.github.srs.model.entity.dynamicentity.dsl

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.{Actuator, DifferentialWheelMotor, Wheel}
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.reward.RewardModel
import io.github.srs.model.entity.dynamicentity.sensor.{ProximitySensor, Sensor}
import io.github.srs.model.entity.{Orientation, Point2D, ShapeType}
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.{StdLightSensors, StdProximitySensors}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AgentDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[ShapeType.Circle, ShapeType.Circle] = CanEqual.derived
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  given CanEqual[Actuator[Agent], Actuator[Agent]] = CanEqual.derived
  given CanEqual[Sensor[Agent, Environment], Sensor[Agent, Environment]] = CanEqual.derived
  given CanEqual[RewardModel[Agent], RewardModel[Agent]] = CanEqual.derived
  given CanEqual[java.util.UUID, java.util.UUID] = CanEqual.derived

  import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*

  val dt: FiniteDuration = FiniteDuration(100, MILLISECONDS)
  val wheelRadius: Double = 0.5

  val wheelMotor: DifferentialWheelMotor[Agent] =
    DifferentialWheelMotor(Wheel(1.0, ShapeType.Circle(wheelRadius)), Wheel(1.0, ShapeType.Circle(wheelRadius)))

  val wheelMotor2: DifferentialWheelMotor[Agent] =
    DifferentialWheelMotor(Wheel(0.5, ShapeType.Circle(wheelRadius)), Wheel(0.2, ShapeType.Circle(wheelRadius)))

  val sensor: Sensor[Agent, Environment] = ProximitySensor(Orientation(0.0), 3.0)

  val sensor2: Sensor[Agent, Environment] = ProximitySensor(Orientation(90.0), 3.0)

  val testReward: RewardModel[Agent] = new RewardModel[Agent]:
    override def evaluate(prev: Environment, current: Environment, entity: Agent, action: Action[?]): Double = 1.0

  "Agent DSL" should "create an agent with default properties" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.*
    val entity = agent
    val _ = entity.position shouldBe DefaultPosition
    val _ = entity.shape shouldBe DefaultShape
    val _ = entity.orientation shouldBe DefaultOrientation
    val _ = entity.actuators shouldBe DefaultActuators
    val _ = entity.sensors shouldBe DefaultSensors
    entity.reward shouldBe DefaultReward

  it should "set the position of the agent" in:
    val pos = Point2D(5.0, 5.0)
    val entity = agent at pos
    entity.position shouldBe pos

  it should "set the shape of the agent" in:
    val shape: ShapeType.Circle = ShapeType.Circle(1.0)
    val entity = agent withShape shape
    entity.shape shouldBe shape

  it should "set the orientation of the agent" in:
    val orientation = Orientation(45.0)
    val entity = agent withOrientation orientation
    entity.orientation shouldBe orientation

  it should "set the actuators of the agent" in:
    val actuators = Seq(wheelMotor)
    val entity = agent withActuators actuators
    entity.actuators shouldBe actuators

  it should "set the sensors of the agent" in:
    val sensors = Seq(sensor)
    val entity = agent withSensors sensors
    entity.sensors shouldBe sensors

  it should "add the actuator using convenience method" in:
    val entity = agent containing wheelMotor
    entity.actuators should contain(wheelMotor)

  it should "add multiple actuators using convenience methods" in:
    val entity = agent containing wheelMotor and wheelMotor2
    entity.actuators should contain allOf (wheelMotor, wheelMotor2)

  it should "add the sensor using convenience method" in:
    val entity = agent containing sensor
    entity.sensors should contain(sensor)

  it should "add multiple sensors using convenience methods" in:
    val entity = agent containing sensor and sensor2
    entity.sensors should contain allOf (sensor, sensor2)

  it should "validate the agent with default properties" in:
    val entity = agent
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "validate the agent with custom properties" in:
    val entity = agent at Point2D(1.0, 2.0) withShape ShapeType.Circle(0.5) withOrientation Orientation(
      90.0,
    ) containing wheelMotor withSensors Seq.empty
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with invalid properties" in:
    val entity = agent containing wheelMotor and wheelMotor2
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "set the reward model of the agent" in:
    val entity = agent withReward testReward
    entity.reward shouldBe testReward

  it should "add proximity sensors using convenience method" in:
    val entity = agent.withProximitySensors
    val _ = entity.sensors should have size 8
    entity.sensors should contain allElementsOf StdProximitySensors

  it should "add light sensors using convenience method" in:
    val entity = agent.withLightSensors
    val _ = entity.sensors should have size 8
    entity.sensors should contain allElementsOf StdLightSensors

  it should "set the speed of the agent's differential wheel motor" in:
    val speed = 2.5
    val entity = agent containing wheelMotor withSpeed speed
    val dwm = entity.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }
    val _ = dwm shouldBe defined
    dwm match
      case Some(motor) =>
        val _ = motor.left.speed shouldBe speed
        motor.right.speed shouldBe speed
      case None => fail("Expected a DifferentialWheelMotor")

  it should "create a differential wheel motor if none exists when setting speed" in:
    val speed = 1.5
    val entity = agent withSpeed speed
    val dwm = entity.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }
    val _ = dwm shouldBe defined
    dwm match
      case Some(motor) =>
        val _ = motor.left.speed shouldBe speed
        motor.right.speed shouldBe speed
      case None => fail("Expected a DifferentialWheelMotor")

  it should "set the id of the agent" in:
    val id = java.util.UUID.randomUUID()
    val entity = agent withId id
    entity.id shouldBe id

  it should "chain multiple DSL operations" in:
    val pos = Point2D(3.0, 4.0)
    val shape: ShapeType.Circle = ShapeType.Circle(0.3)
    val orientation = Orientation(180.0)
    val entity = agent at pos withShape shape withOrientation orientation containing wheelMotor withSensor sensor withReward testReward

    val _ = entity.position shouldBe pos
    val _ = entity.shape shouldBe shape
    val _ = entity.orientation shouldBe orientation
    val _ = entity.actuators should contain(wheelMotor)
    val _ = entity.sensors should contain(sensor)
    entity.reward shouldBe testReward

  it should "validate agent with out of bounds radius" in:
    val invalidShape: ShapeType.Circle = ShapeType.Circle(10.0) // Exceeds MaxRadius
    val entity = agent withShape invalidShape
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "validate agent with negative radius" in:
    val invalidShape: ShapeType.Circle = ShapeType.Circle(-0.5)
    val entity = agent withShape invalidShape
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "validate agent with NaN position" in:
    val invalidPos = Point2D(Double.NaN, 5.0)
    val entity = agent at invalidPos
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "validate agent with infinite position" in:
    val invalidPos = Point2D(Double.PositiveInfinity, 5.0)
    val entity = agent at invalidPos
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

end AgentDslTest