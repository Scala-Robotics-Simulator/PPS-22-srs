package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity, Robot }
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.compatible.Assertion

class ProximitySensorTest extends AnyFlatSpec with Matchers:

  val offset: Orientation = Orientation(0.0)
  val distance: Double = 0.5
  val range: Double = 5.0
  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(offset, distance, range).toOption.value

  val pointingDownSensor: ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(270), 0.5, 5.0).toOption.value

  val pointingBackwardSensor: ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(180), 0.5, 5.0).toOption.value

  val pointingLeftSensor: ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(90), 0.5, 5.0).toOption.value

  val robot: Robot = Robot(
    position = Point2D(0.5, 1),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = SensorSuite(sensor, pointingDownSensor, pointingBackwardSensor, pointingLeftSensor),
  ).toOption.value

  private def createObstacle(position: Point2D, width: Double = 1.0, height: Double = 1.0): Obstacle =
    Obstacle(position, Orientation(0.0), width, height)

  private def createRobot(position: Point2D): Robot =
    Robot(
      position = position,
      shape = ShapeType.Circle(0.5),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value

  private def createEnvironment(entities: Set[Entity]): Environment =
    Environment(
      width = 10.0,
      height = 10.0,
      entities = entities,
    ).toOption.value

  private def testSensorReading(
      sensor: ProximitySensor[DynamicEntity, Environment],
      entities: Set[Entity],
      expectedReading: Double,
  ): Assertion =
    val environment = createEnvironment(entities + robot)
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be(expectedReading)

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
    val obstacle = createObstacle(Point2D(1.5, 1))
    testSensorReading(sensor, Set(obstacle), expectedReading = 0.0)

  it should "not sense an obstacle outside its range" in:
    val farObstacle = createObstacle(Point2D(9.0, 1.0))
    testSensorReading(sensor, Set(farObstacle), expectedReading = 1.0)

  it should "be able to sense a robot in front" in:
    val otherRobot = createRobot(Point2D(1.5, 1))
    testSensorReading(sensor, Set(otherRobot), expectedReading = 0.0)

  it should "not sense a robot outside its range" in:
    val farRobot = createRobot(Point2D(9.0, 1))
    testSensorReading(sensor, Set(farRobot), expectedReading = 1.0)

  it should "sense an obstacle directly below" in:
    val obstacleBelow = createObstacle(Point2D(0.5, 2.0))
    testSensorReading(pointingDownSensor, Set(obstacleBelow), expectedReading = 0.0)

  it should "not sense an obstacle below outside its range" in:
    val farObstacleBelow = createObstacle(Point2D(0.5, 7.0))
    testSensorReading(pointingDownSensor, Set(farObstacleBelow), expectedReading = 1.0)

  it should "be able to sense a robot directly below" in:
    val robotBelow = createRobot(Point2D(0.5, 2.0))
    testSensorReading(pointingDownSensor, Set(robotBelow), expectedReading = 0.0)

  it should "not sense a robot below outside its range" in:
    val farRobotBelow = createRobot(Point2D(0.5, 7.0))
    testSensorReading(pointingDownSensor, Set(farRobotBelow), expectedReading = 1.0)

  it should "sense an obstacle behind the robot" in:
    val obstacleBehind = createObstacle(Point2D(-0.5, 1.0))
    testSensorReading(pointingBackwardSensor, Set(obstacleBehind), expectedReading = 0.0)

  it should "not sense an obstacle behind outside its range" in:
    val farObstacleBehind = createObstacle(Point2D(-6.0, 1.0))
    testSensorReading(pointingBackwardSensor, Set(farObstacleBehind), expectedReading = 1.0)

  it should "be able to sense a robot behind" in:
    val robotBehind = createRobot(Point2D(-0.5, 1))
    testSensorReading(pointingBackwardSensor, Set(robotBehind), expectedReading = 0.0)

  it should "not sense a robot behind outside its range" in:
    val farRobotBehind = createRobot(Point2D(-6.0, 1))
    testSensorReading(pointingBackwardSensor, Set(farRobotBehind), expectedReading = 1.0)

  it should "sense an obstacle above the robot with left-pointing sensor" in:
    val obstacleAbove = createObstacle(Point2D(0.5, 0.0))
    testSensorReading(pointingLeftSensor, Set(obstacleAbove), expectedReading = 0.0)

  it should "not sense an obstacle above outside the left sensor's range" in:
    val farObstacleAbove = createObstacle(Point2D(0.5, -6.0))
    testSensorReading(pointingLeftSensor, Set(farObstacleAbove), expectedReading = 1.0)

  it should "be able to sense a robot above with left-pointing sensor" in:
    val robotAbove = createRobot(Point2D(0.5, 0.0))
    testSensorReading(pointingLeftSensor, Set(robotAbove), expectedReading = 0.0)

  it should "not sense a robot above outside the left sensor's range" in:
    val farRobotAbove = createRobot(Point2D(0.5, -6.0))
    testSensorReading(pointingLeftSensor, Set(farRobotAbove), expectedReading = 1.0)

end ProximitySensorTest
