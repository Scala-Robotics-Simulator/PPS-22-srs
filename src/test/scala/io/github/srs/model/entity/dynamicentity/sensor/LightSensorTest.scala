package io.github.srs.model.entity.dynamicentity.sensor

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.SimulationDefaults.StaticEntity.Light.{ DefaultOrientation, DefaultRadius }
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LightSensorTest extends AnyFlatSpec with Matchers:

  given CanEqual[Orientation, Orientation] = CanEqual.derived

  val offset: Orientation = Orientation(0.0)
  val range: Double = 5.0
  val sensor: LightSensor[DynamicEntity, Environment] = LightSensor(offset)

  val pointingDownSensor: LightSensor[DynamicEntity, Environment] = createSensor(270)
  val pointingBackwardSensor: LightSensor[DynamicEntity, Environment] = createSensor(180)
  val pointingLeftSensor: LightSensor[DynamicEntity, Environment] = createSensor(90)

  // Diagonal sensors
  val pointingNorthEastSensor: LightSensor[DynamicEntity, Environment] = createSensor(45)
  val pointingSouthEastSensor: LightSensor[DynamicEntity, Environment] = createSensor(315)
  val pointingSouthWestSensor: LightSensor[DynamicEntity, Environment] = createSensor(225)
  val pointingNorthWestSensor: LightSensor[DynamicEntity, Environment] = createSensor(135)

  val robot: Robot = Robot(
    position = Point2D(6.0, 6.0),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = Vector(sensor, pointingDownSensor, pointingBackwardSensor, pointingLeftSensor),
  ).validate.toOption.value

  private def createSensor(orientationDegrees: Double): LightSensor[DynamicEntity, Environment] =
    LightSensor(Orientation(orientationDegrees))

  private def createLight(
      position: Point2D,
      illuminationRadius: Double = 5,
      intensity: Double = 1,
      attenuation: Double = 1,
  ): Light =
    Light(
      pos = position,
      orient = DefaultOrientation,
      radius = DefaultRadius,
      illuminationRadius = illuminationRadius,
      intensity = intensity,
      attenuation = attenuation,
    )

  private def createEnvironment(entities: List[Entity], width: Int = 20, height: Int = 20): Environment =
    (environment
      withWidth width
      withHeight height
      containing entities).validate
      .fold(err => fail(s"Environment invalid in test fixture: $err"), identity)

  private def getSensorReading(
      sensor: LightSensor[DynamicEntity, Environment],
      entities: List[Entity],
  ): Double =
    val environment = createEnvironment(robot :: entities)
    sensor.sense[IO](robot, environment).unsafeRunSync()

  "LightSensor" should "sense correctly an environment without light" in:
    val reading = getSensorReading(sensor, List.empty)
    reading should be(0.0)

  it should "sense correctly a nearby light" in:
    import Point2D.*
    val light = createLight(robot.position + (0.6, 0))
    val reading = getSensorReading(sensor, List(light))
    reading should be > 0.95

  it should "read a lower value for a distant light" in:
    import Point2D.*
    val light = createLight(robot.position + (3.0, 0))
    val reading = getSensorReading(sensor, List(light))
    reading should be < 0.6

  it should "read a lower value for a light with lower intensity" in:
    import Point2D.*
    val light = createLight(robot.position + (0.6, 0), intensity = 0.5)
    val reading = getSensorReading(sensor, List(light))
    reading should be < 0.9

  it should "read a lower value from the diagonal sensor pointing to a light" in:
    import Point2D.*
    val light = createLight(robot.position + (0.6, 0))
    val reading = getSensorReading(pointingNorthEastSensor, List(light))
    reading should be < 0.95

  it should "not see the light from the diagonal sensor pointing away from a light" in:
    import Point2D.*
    val light = createLight(robot.position + (0.6, 0))
    val reading = getSensorReading(pointingSouthWestSensor, List(light))
    reading should be(0.0)

  it should "not see the light from the sensor pointing away from a light" in:
    import Point2D.*
    val light = createLight(robot.position + (0.6, 0))
    val reading = getSensorReading(pointingBackwardSensor, List(light))
    reading should be(0.0)

end LightSensorTest
