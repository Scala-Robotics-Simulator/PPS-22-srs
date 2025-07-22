package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.PositiveDouble
import io.github.srs.model.entity.dynamicentity.{ Actuator, Robot }
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProximitySensorTest extends AnyFlatSpec with Matchers:
  given CanEqual[ProximitySensor, ProximitySensor] = CanEqual.derived

  val offset: Orientation = Orientation(0.0)
  val distance: Distance = PositiveDouble(0.5).toOption.value
  val range: Range = PositiveDouble(5.0).toOption.value
  val sensor: ProximitySensor = ProximitySensor(offset, distance, range)

  val robot: Robot = Robot(
    position = Point2D(0.5, 1),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = SensorSuite(sensor),
  ).toOption.value

  "ProximitySensor" should "have a valid offset, distance, and range" in:
    val sensor = ProximitySensor(offset, distance, range)
    (sensor.offset, sensor.distance, sensor.range) should be(
      (offset, distance, range),
    )

  it should "be able to sense an obstacle in front" in:
    val obstacle: Obstacle = Obstacle((1.5, 1.5), Orientation(0.0), 1.0, 1.0)
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

end ProximitySensorTest
