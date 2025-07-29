package io.github.srs.model.entity.dynamicentity.sensor

import scala.language.postfixOps

import cats.Id
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.{ DynamicEntity, Robot }
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

class ProximitySensorTest extends AnyFlatSpec with Matchers:

  val offset: Orientation = Orientation(0.0)
  val distance: Double = 0.5
  val range: Double = 5.0
  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(offset, distance, range)

  val pointingDownSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(270)
  val pointingBackwardSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(180)
  val pointingLeftSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(90)

  // Diagonal sensors
  val pointingNorthEastSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(45)
  val pointingSouthEastSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(315)
  val pointingSouthWestSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(225)
  val pointingNorthWestSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(135)

  val robot: Robot = Robot(
    position = Point2D(6.0, 6.0),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = Vector(sensor, pointingDownSensor, pointingBackwardSensor, pointingLeftSensor),
  ).validate.toOption.value

  private def createSensor(orientationDegrees: Double): ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(orientationDegrees), 0.5, 5.0)

  private def createObstacle(
      position: Point2D,
      orientation: Orientation = Orientation(0.0),
      width: Double = 1.0,
      height: Double = 1.0,
  ): Obstacle =
    Obstacle(position, orientation, width, height)

  private def createRobot(position: Point2D, orientation: Orientation = Orientation(0.0), radius: Double = 0.5): Robot =
    robot at position withShape ShapeType.Circle(radius) withOrientation orientation

  private def createEnvironment(entities: Set[Entity], width: Int = 20, height: Int = 20): Environment =
    (environment
      withWidth width
      withHeight height
      containing entities).validate
      .fold(err => fail(s"Environment invalid in test fixture: $err"), identity)

  private def getSensorReading(
      sensor: ProximitySensor[DynamicEntity, Environment],
      entities: Set[Entity],
  ): Double =
    val environment = createEnvironment(entities + robot)
    sensor.sense[Id](robot, environment)

  "ProximitySensor" should "have a valid offset, distance, and range" in:
    inside(ProximitySensor(offset, distance, range).validate):
      case Right(sensor) =>
        (sensor.offset, sensor.distance, sensor.range) should be(
          (offset, distance, range),
        )

  it should "not be able to create a sensor with negative distance" in:
    inside(ProximitySensor(offset, -1.0, range).validate):
      case Left(error: DomainError) =>
        error shouldBe a[DomainError.NegativeOrZero]

  it should "not be able to create a sensor with negative range" in:
    inside(ProximitySensor(offset, distance, -1.0).validate):
      case Left(error: DomainError) =>
        error shouldBe a[DomainError.NegativeOrZero]

  it should "be able to sense an obstacle in front" in:
    val obstacle = createObstacle(Point2D(7.001, 6.0))
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be < 0.01

  it should "not sense an obstacle that is a bit lower" in:
    val obstacle = createObstacle(Point2D(7.5, 6.5), height = .99)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "not sense an obstacle that is a bit higher" in:
    val obstacle = createObstacle(Point2D(7.5, 5.5), height = .99)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "sense an obstacle that is very narrow" in:
    val obstacle = createObstacle(Point2D(7.5, 6.0), width = 0.01)
    val environment = createEnvironment(Set(robot, obstacle))
    val sensorReading = sensor.sense[Id](robot, environment)
    sensorReading should be < 1.0 // Should detect the narrow obstacle

  it should "not sense an obstacle positioned slightly to the side" in:
    val obstacle = createObstacle(Point2D(7.5, 5.49), width = 0.5)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "sense an obstacle at the exact edge of sensor range" in:
    val obstacle = createObstacle(Point2D(11.99, 6.0))
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be > 0.9

  it should "not sense an obstacle just outside sensor range" in:
    val obstacle = createObstacle(Point2D(12.01, 6.0))
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "not sense an obstacle outside its range" in:
    val farObstacle = createObstacle(Point2D(12.0, 6.0))
    val reading = getSensorReading(sensor, Set(farObstacle))
    reading should be(1.0)

  it should "be able to sense a robot in front" in:
    val otherRobot = createRobot(Point2D(7.001, 6.0))
    val reading = getSensorReading(sensor, Set(otherRobot))
    reading should be < 0.01

  it should "not sense a robot outside its range" in:
    val farRobot = createRobot(Point2D(12.0, 6.0))
    val reading = getSensorReading(sensor, Set(farRobot))
    reading should be(1.0)

  it should "not sense a robot that is positioned slightly above" in:
    val robotAbove = createRobot(Point2D(7.5, 5.49))
    val reading = getSensorReading(sensor, Set(robotAbove))
    reading should be(1.0)

  it should "not sense a robot that is positioned slightly below" in:
    val robotBelow = createRobot(Point2D(7.5, 6.51))
    val reading = getSensorReading(sensor, Set(robotBelow))
    reading should be(1.0)

  it should "sense a robot at the exact edge of vertical alignment" in:
    val robotAtEdge = createRobot(Point2D(7.5, 6.0))
    val reading = getSensorReading(sensor, Set(robotAtEdge))
    reading should be(0.1)

  it should "sense a very small robot in front" in:
    val smallRobot = Robot(
      position = Point2D(7.5, 6.0),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = Vector.empty[Sensor[Robot, Environment]],
    ).validate.toOption.value
    val reading = getSensorReading(sensor, Set(smallRobot))
    reading should be < 0.2 // Should detect the small robot close

  it should "sense a robot at the maximum range boundary" in:
    val robotAtMaxRange = createRobot(Point2D(11.99, 6.0))
    val reading = getSensorReading(sensor, Set(robotAtMaxRange))
    reading should be > 0.99 // Should detect the robot at the edge of range

  it should "sense an obstacle directly below" in:
    val obstacleBelow = createObstacle(Point2D(6.0, 7.01))
    val reading = getSensorReading(pointingDownSensor, Set(obstacleBelow))
    reading should be < 0.01 // Should detect the obstacle directly below

  it should "not sense an obstacle below outside its range" in:
    val farObstacleBelow = createObstacle(Point2D(6.0, 12.0))
    val reading = getSensorReading(pointingDownSensor, Set(farObstacleBelow))
    reading should be(1.0)

  it should "be able to sense a robot directly below" in:
    val robotBelow = createRobot(Point2D(6.0, 7.01))
    val reading = getSensorReading(pointingDownSensor, Set(robotBelow))
    reading should be < 0.01

  it should "not sense a robot below outside its range" in:
    val farRobotBelow = createRobot(Point2D(6.0, 12.0))
    val reading = getSensorReading(pointingDownSensor, Set(farRobotBelow))
    reading should be(1.0)

  it should "not sense an obstacle positioned slightly to the left relative to the downward sensor" in:
    val obstacleLeft = createObstacle(Point2D(6.51, 7.5), width = 0.5)
    val reading = getSensorReading(pointingDownSensor, Set(obstacleLeft))
    reading should be(1.0)

  it should "sense an obstacle at the exact horizontal alignment below" in:
    val obstacleAligned = createObstacle(Point2D(6.0, 7.0), height = 0.5)
    val reading = getSensorReading(pointingDownSensor, Set(obstacleAligned))
    reading should be < 0.1 // Should detect the obstacle directly below

  it should "sense a very thin obstacle below" in:
    val thinObstacle = createObstacle(Point2D(6.0, 7.01), width = 0.1)
    val reading = getSensorReading(pointingDownSensor, Set(thinObstacle))
    reading should be < 0.01 // Should detect the thin obstacle below

  it should "not sense an obstacle above outside the left sensor's range" in:
    val farObstacleAbove = createObstacle(Point2D(6.0, 0.1), height = 0.1)
    val reading = getSensorReading(pointingLeftSensor, Set(farObstacleAbove))
    reading should be(1.0)

  it should "not sense a robot above outside the left sensor's range" in:
    val farRobotAbove = createRobot(Point2D(6.0, 0.3), radius = 0.19)
    val reading = getSensorReading(pointingLeftSensor, Set(farRobotAbove))
    reading should be(1.0)

  // Edge case tests for multiple entities
  it should "sense the closest obstacle when multiple obstacles are present" in:
    val closeObstacle = createObstacle(Point2D(7.01, 6.0))
    val farObstacle = createObstacle(Point2D(8.5, 6.0))
    val reading = getSensorReading(sensor, Set(closeObstacle, farObstacle))
    reading should be < 0.01 // Should detect the closer obstacle

  it should "not sense anything when obstacles are positioned outside sensor path" in:
    val obstacleAbove = createObstacle(Point2D(7.5, 5.0))
    val obstacleBelow = createObstacle(Point2D(7.5, 7.0))
    val reading = getSensorReading(sensor, Set(obstacleAbove, obstacleBelow))
    reading should be(1.0)

  it should "sense mixed entities (robot and obstacle) with robot being closer" in:
    val closeRobot = createRobot(Point2D(7.001, 6.0))
    val farObstacle = createObstacle(Point2D(8.5, 6.0))
    val reading = getSensorReading(sensor, Set(closeRobot, farObstacle))
    reading should be < 0.01

  it should "sense mixed entities (robot and obstacle) with obstacle being closer" in:
    val closeObstacle = createObstacle(Point2D(7.01, 6.0))
    val farRobot = createRobot(Point2D(8.5, 6.0))
    val reading = getSensorReading(sensor, Set(closeObstacle, farRobot))
    reading should be < 0.01

  it should "handle empty environment correctly" in:
    val reading = getSensorReading(sensor, Set.empty)
    reading should be(1.0)

  // Diagonal sensor tests
  it should "sense an obstacle in the northeast diagonal" in:
    val obstacleNE =
      createObstacle(Point2D(7.0, 5.25), height = 0.5) // Positioned along NE diagonal from sensor
    val environment = createEnvironment(Set(robot, obstacleNE))
    val sensorReading = pointingNorthEastSensor.sense[Id](robot, environment)
    sensorReading should be < 0.05 // Should detect obstacle very close

  it should "not sense an obstacle outside northeast sensor range" in:
    val farObstacleNE = createObstacle(Point2D(10.5, 1.5))
    val reading = getSensorReading(pointingNorthEastSensor, Set(farObstacleNE))
    reading should be(1.0)

  it should "sense an obstacle in the southeast diagonal" in:
    val obstacleSE =
      createObstacle(Point2D(7.0, 6.75), height = 0.5) // Positioned along SE diagonal from sensor
    val environment = createEnvironment(Set(robot, obstacleSE))
    val sensorReading = pointingSouthEastSensor.sense[Id](robot, environment)
    sensorReading should be < 0.05 // Should detect obstacle (return value less than 1.0)

  it should "not sense an obstacle outside southeast sensor range" in:
    val farObstacleSE = createObstacle(Point2D(10.5, 10.5))
    val reading = getSensorReading(pointingSouthEastSensor, Set(farObstacleSE))
    reading should be(1.0)

  // Tests with different robot orientations
  it should "sense correctly when robot is rotated 90 degrees" in:
    val rotatedRobot = createRobot(Point2D(7.5, 7.01), Orientation(90))
    val obstacleNorth = createObstacle(Point2D(7.5, 6.0)) // Place obstacle to the north
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleNorth))

    // Forward sensor (offset 0) should now point north due to 90-degree rotation
    val forwardSensorReading = sensor.sense[Id](rotatedRobot, envWithObstacle)
    forwardSensorReading should be < 0.01 // Should detect obstacle to the north

  it should "sense correctly when robot is rotated 180 degrees" in:
    val rotatedRobot = createRobot(Point2D(8.01, 6.0), Orientation(180))
    val obstacleInFront = createObstacle(Point2D(7.0, 6.0))
    val envWithObstacles = createEnvironment(Set(rotatedRobot, obstacleInFront))

    // Forward sensor should now point backward due to 180-degree rotation
    val forwardSensorReading = sensor.sense[Id](rotatedRobot, envWithObstacles)
    forwardSensorReading should be < 0.01 // Should detect obstacle that's now "in front"

  it should "sense correctly when robot is rotated 270 degrees" in:
    val rotatedRobot = createRobot(Point2D(6.0, 6.0), Orientation(270))
    val obstacleSouth = createObstacle(Point2D(6.0, 7.01)) // Place obstacle to the south
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleSouth))

    // Forward sensor should now point south due to 270-degree rotation
    val forwardSensorReading = sensor.sense[Id](rotatedRobot, envWithObstacle)
    forwardSensorReading should be < 0.01 // Should detect obstacle to the south

  it should "sense correctly when obstacle is rotated 90 degrees" in:
    // Horizontal obstacle rotated to vertical
    val rotatedObstacle = createObstacle(Point2D(7.01, 7), orientation = Orientation(90), width = 2)
    val envWithRotatedObstacle = createEnvironment(Set(robot, rotatedObstacle))

    // If the obstacle is vertical, the forward sensor should detect it
    // since it is now aligned with the robot's forward direction
    val forwardSensorReading = sensor.sense[Id](robot, envWithRotatedObstacle)
    forwardSensorReading should be < 0.01 // Should detect the rotated obstacle

  it should "sense correctly when obstacle is rotated 180 degrees" in:
    // Horizontal obstacle rotated to horizontal (no change)
    val rotatedObstacle = createObstacle(Point2D(7.01, 6.0), orientation = Orientation(180), height = 2)
    val envWithRotatedObstacle = createEnvironment(Set(robot, rotatedObstacle))

    // Forward sensor should still detect the obstacle
    val forwardSensorReading = sensor.sense[Id](robot, envWithRotatedObstacle)
    forwardSensorReading should be < 0.01 // Should detect the rotated obstacle

  it should "sense correctly when obstacle is rotated 270 degrees" in:
    // Horizontal obstacle rotated to vertical
    val rotatedObstacle = createObstacle(Point2D(7.01, 6), orientation = Orientation(270), width = 2)
    val envWithRotatedObstacle = createEnvironment(Set(robot, rotatedObstacle))

    // Forward sensor should now point south due to 270-degree rotation
    val forwardSensorReading = sensor.sense[Id](robot, envWithRotatedObstacle)
    forwardSensorReading should be < 0.01 // Should detect the rotated obstacle

  it should "sense an obstacle rotated by 45 degrees" in:
    val rotatedObstacle =
      createObstacle(Point2D(7.0, 5.25), orientation = Orientation(45), height = 0.5) // Northeast diagonal
    val envWithRotatedObstacle = createEnvironment(Set(robot, rotatedObstacle))

    // Forward sensor should now point northeast due to 45-degree rotation
    val forwardSensorReading = pointingNorthEastSensor.sense[Id](robot, envWithRotatedObstacle)
    forwardSensorReading should be < 0.1 // Should detect obstacle in the northeast direction

  it should "sense an obstacle rotated by 45 degrees at the edge of its range" in:
    val farRotatedObstacle = createObstacle(Point2D(10, 2), orientation = Orientation(45))
    val reading = getSensorReading(pointingNorthEastSensor, Set(farRotatedObstacle))
    val _ = reading should be > 0.9
    reading should be < 1.0 // Should detect very close to range boundary

  it should "not sense an obstacle rotated by 45 degrees outside its range" in:
    val farRotatedObstacle = createObstacle(Point2D(10.5, 1.5), orientation = Orientation(45))
    val reading = getSensorReading(pointingNorthEastSensor, Set(farRotatedObstacle))
    reading should be(1.0) // Should not detect obstacle outside range

  it should "sense correctly when robot is rotated 45 degrees" in:
    val rotatedRobot = createRobot(Point2D(6.0, 6.0), Orientation(45))
    val obstacleDiagonal = createObstacle(Point2D(7.0, 5.25), height = 0.5) // Northeast diagonal
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleDiagonal))

    // Forward sensor should now point northeast due to 45-degree rotation
    val forwardSensorReading = sensor.sense[Id](rotatedRobot, envWithObstacle)
    forwardSensorReading should be < 0.05 // Should detect obstacle in the northeast direction

  it should "handle multiple obstacles at different angles with rotated robot" in:
    val rotatedRobot = createRobot(Point2D(6.0, 6.0), Orientation(90))
    val obstacleNorth = createObstacle(Point2D(6.01, 4.99))
    val obstacleEast = createObstacle(Point2D(7.01, 6.0))
    val obstacleSouth = createObstacle(Point2D(5.99, 7.01))
    val obstacleWest = createObstacle(Point2D(4.99, 6.01))

    val envWithObstacles = createEnvironment(
      Set(
        rotatedRobot,
        obstacleNorth,
        obstacleEast,
        obstacleSouth,
        obstacleWest,
      ),
    )

    // With 90-degree rotation: forward points north, backward points south,
    // left points west, down points east
    val forwardReading = sensor.sense[Id](rotatedRobot, envWithObstacles)
    val backwardReading = pointingBackwardSensor.sense[Id](rotatedRobot, envWithObstacles)
    val leftReading = pointingLeftSensor.sense[Id](rotatedRobot, envWithObstacles)
    val downReading = pointingDownSensor.sense[Id](rotatedRobot, envWithObstacles)

    // All sensors should detect obstacles
    Seq(forwardReading, backwardReading, leftReading, downReading).forall(_ < 0.01) should be(true)

  // Additional precision and boundary edge cases
  it should "handle floating point precision at range boundaries" in:
    val precisionObstacle = createObstacle(Point2D(11.9900001, 6.0))
    val reading = getSensorReading(sensor, Set(precisionObstacle))
    val _ = reading should be > 0.99
    reading should be < 1.0 // Should detect very close to range boundary

  it should "not sense obstacles at exactly distance + range boundary" in:
    val boundaryObstacle = createObstacle(Point2D(12.0000001, 6.0))
    val reading = getSensorReading(sensor, Set(boundaryObstacle))
    reading should be(1.0)

  it should "handle very small robots at edge of detection" in:
    val tinyRobot = Robot(
      position = Point2D(11.49, 6.0),
      shape = ShapeType.Circle(0.01),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = Vector.empty[Sensor[Robot, Environment]],
    ).validate.toOption.value
    val reading = getSensorReading(sensor, Set(tinyRobot))
    reading should be > 0.99

  it should "handle obstacles with extreme aspect ratios" in:
    val tallThinObstacle = createObstacle(Point2D(7.5, 6.0), width = 0.001, height = 10.0)
    val environment = createEnvironment(Set(robot, tallThinObstacle))
    val sensorReading = sensor.sense[Id](robot, environment)
    sensorReading should be < 1.0 // Should detect very thin obstacle

  it should "sense environment boundaries correctly" in:
    val robot = createRobot(Point2D(1, 1), Orientation(0.0))
    val environment = createEnvironment(Set(robot), width = 2, height = 2)
    val sensorReadings = Seq(
      sensor.sense[Id](robot, environment),
      pointingDownSensor.sense[Id](robot, environment),
      pointingBackwardSensor.sense[Id](robot, environment),
      pointingLeftSensor.sense[Id](robot, environment),
    )
    sensorReadings.forall(_ == 0.1) should be(true) // Should detect boundaries

  it should "sense environment boundaries with rotated robot" in:
    val rotatedRobot = createRobot(Point2D(1, 1), Orientation(90))
    val environment = createEnvironment(Set(rotatedRobot), width = 2, height = 2)
    val sensorReadings = Seq(
      sensor.sense[Id](rotatedRobot, environment),
      pointingDownSensor.sense[Id](rotatedRobot, environment),
      pointingBackwardSensor.sense[Id](rotatedRobot, environment),
      pointingLeftSensor.sense[Id](rotatedRobot, environment),
    )
    sensorReadings.forall(_ == 0.1) should be(true) // Should detect boundaries

  it should "sense environment boundaries with diagonal sensors" in:
    val rotatedRobot = createRobot(Point2D(1, 1), Orientation(45))
    val environment = createEnvironment(Set(rotatedRobot), width = 2, height = 2)
    val sensorReadings = Seq(
      pointingNorthEastSensor.sense[Id](rotatedRobot, environment),
      pointingSouthEastSensor.sense[Id](rotatedRobot, environment),
      pointingSouthWestSensor.sense[Id](rotatedRobot, environment),
      pointingNorthWestSensor.sense[Id](rotatedRobot, environment),
    )
    sensorReadings.forall(_ == 0.1) should be(true) // Should detect boundaries

  it should "detect boundaries at the edge of the sensor range" in:
    val edgeRobot = createRobot(Point2D(5.6, 5.6), Orientation(0.0))
    val environment = createEnvironment(Set(edgeRobot), width = 11, height = 11)
    val closeEnoughSensors = Seq(
      sensor.sense[Id](edgeRobot, environment),
      pointingDownSensor.sense[Id](edgeRobot, environment),
    )
    val notCloseEnoughSensors = Seq(
      pointingBackwardSensor.sense[Id](edgeRobot, environment),
      pointingLeftSensor.sense[Id](edgeRobot, environment),
    )
    val _ =
      closeEnoughSensors.forall(reading => reading > 0.9 && reading < 1) should be(true) // Should detect boundaries
    notCloseEnoughSensors.forall(_ == 1.0) should be(true) // Should not detect boundaries

end ProximitySensorTest
