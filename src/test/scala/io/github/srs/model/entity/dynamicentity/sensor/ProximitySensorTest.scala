package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity, Robot }
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProximitySensorTest extends AnyFlatSpec with Matchers:

  given CanEqual[ProximitySensor[DynamicEntity, Environment], ProximitySensor[DynamicEntity, Environment]] =
    CanEqual.derived

  val offset: Orientation = Orientation(0.0)
  val distance: Double = 0.5
  val range: Double = 5.0
  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(offset, distance, range).toOption.value

  val pointingDownSensor: ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(270), 0.5, 5.0).toOption.value

  val robot: Robot = Robot(
    position = Point2D(0.5, 1),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = SensorSuite(sensor, pointingDownSensor),
  ).toOption.value

  "ProximitySensor" should "have a valid offset, distance, and range" in:
    inside(ProximitySensor(offset, distance, range)):
      case Right(sensor) =>
        (sensor.offset, sensor.distance, sensor.range) should be(
          (offset, distance, range),
        )

  it should "not be able to create a sensor with negative distance" in:
    inside(ProximitySensor(offset, -1.0, range)):
      case Left(error: DomainError) =>
        error shouldBe a[DomainError.NegativeOrZero]

  it should "not be able to create a sensor with negative range" in:
    inside(ProximitySensor(offset, distance, -1.0)):
      case Left(error: DomainError) =>
        error shouldBe a[DomainError.NegativeOrZero]

  it should "be able to sense an obstacle in front" in:
    val obstacle: Obstacle = Obstacle((1.5, 1), Orientation(0.0), 1.0, 1.0)
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(obstacle, robot),
    ).toOption.value
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be(0.0)

  it should "not sense an obstacle outside its range" in:
    val farObstacle: Obstacle = Obstacle((9.0, 1.0), Orientation(0.0), 1.0, 1.0)
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(farObstacle, robot),
    ).toOption.value
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be(1.0)

  it should "be able to sense a robot in front" in:
    val otherRobot: Robot = Robot(
      position = Point2D(1.5, 1),
      shape = ShapeType.Circle(0.5),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(otherRobot, robot),
    ).toOption.value
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be(0.0)

  it should "not sense a robot outside its range" in:
    val farRobot: Robot = Robot(
      position = Point2D(9.0, 1),
      shape = ShapeType.Circle(0.5),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(farRobot, robot),
    ).toOption.value
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be(1.0)

  it should "sense an obstacle directly below" in:
    val obstacleBelow: Obstacle = Obstacle((0.5, 2.0), Orientation(0.0), 1.0, 1.0)
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(obstacleBelow, robot),
    ).toOption.value
    val sensorReading = pointingDownSensor.sense(robot)(environment)
    sensorReading should be(0.0)

  it should "not sense an obstacle below outside its range" in:
    val farObstacleBelow: Obstacle = Obstacle((0.5, 7.0), Orientation(0.0), 1.0, 1.0)
    val environment: Environment = Environment(
      width = 10.0,
      height = 10.0,
      entities = Set(farObstacleBelow, robot),
    ).toOption.value
    val sensorReading = pointingDownSensor.sense(robot)(environment)
    sensorReading should be(1.0)

end ProximitySensorTest
