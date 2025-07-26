package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity, Robot }
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProximitySensorTest extends AnyFlatSpec with Matchers:

  val offset: Orientation = Orientation(0.0)
  val distance: Double = 0.5
  val range: Double = 5.0
  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(offset, distance, range).toOption.value

  val pointingDownSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(270)
  val pointingBackwardSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(180)
  val pointingLeftSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(90)

  // Diagonal sensors
  val pointingNorthEastSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(45)
  val pointingSouthEastSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(315)
  val pointingSouthWestSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(225)
  val pointingNorthWestSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(135)

  val robot: Robot = Robot(
    position = Point2D(0.5, 1),
    shape = ShapeType.Circle(0.5),
    orientation = Orientation(0.0),
    actuators = Seq.empty[Actuator[Robot]],
    sensors = SensorSuite(sensor, pointingDownSensor, pointingBackwardSensor, pointingLeftSensor),
  ).toOption.value

  private def createSensor(orientationDegrees: Double): ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(orientationDegrees), 0.5, 5.0).toOption.value

  private def createObstacle(position: Point2D, width: Double = 1.0, height: Double = 1.0): Obstacle =
    Obstacle(position, Orientation(0.0), width, height)

  private def createRobot(position: Point2D, orientation: Orientation = Orientation(0.0)): Robot =
    Robot(
      position = position,
      shape = ShapeType.Circle(0.5),
      orientation = orientation,
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value

  private def createEnvironment(entities: Set[Entity]): Environment =
    Environment(
      width = 10.0,
      height = 10.0,
      entities = entities,
    ).toOption.value

  private def getSensorReading(
      sensor: ProximitySensor[DynamicEntity, Environment],
      entities: Set[Entity],
  ): Double =
    val environment = createEnvironment(entities + robot)
    sensor.sense(robot)(environment)

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
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(0.0)

  it should "not sense an obstacle that is a bit lower" in:
    val obstacle = createObstacle(Point2D(2, 1.5), height = .99)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "not sense an obstacle that is a bit higher" in:
    val obstacle = createObstacle(Point2D(2, 0.5), height = .99)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "sense an obstacle that is very narrow" in:
    val obstacle = createObstacle(Point2D(2, 1), width = 0.01, height = 1.0)
    val environment = createEnvironment(Set(robot, obstacle))
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be < 1.0 // Should detect the narrow obstacle

  it should "not sense an obstacle positioned slightly to the side" in:
    val obstacle = createObstacle(Point2D(2, 0.49), width = 0.5, height = 1.0)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "sense an obstacle at the exact edge of sensor range" in:
    val obstacle = createObstacle(Point2D(6.49, 1))
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be > 0.9

  it should "not sense an obstacle just outside sensor range" in:
    val obstacle = createObstacle(Point2D(6.51, 1), width = 1.0, height = 1.0)
    val reading = getSensorReading(sensor, Set(obstacle))
    reading should be(1.0)

  it should "not sense an obstacle outside its range" in:
    val farObstacle = createObstacle(Point2D(9.0, 1.0))
    val reading = getSensorReading(sensor, Set(farObstacle))
    reading should be(1.0)

  it should "be able to sense a robot in front" in:
    val otherRobot = createRobot(Point2D(1.5, 1))
    val reading = getSensorReading(sensor, Set(otherRobot))
    reading should be(0.0)

  it should "not sense a robot outside its range" in:
    val farRobot = createRobot(Point2D(9.0, 1))
    val reading = getSensorReading(sensor, Set(farRobot))
    reading should be(1.0)

  it should "not sense a robot that is positioned slightly above" in:
    val robotAbove = createRobot(Point2D(2, 0.49))
    val reading = getSensorReading(sensor, Set(robotAbove))
    reading should be(1.0)

  it should "not sense a robot that is positioned slightly below" in:
    val robotBelow = createRobot(Point2D(2, 1.51))
    val reading = getSensorReading(sensor, Set(robotBelow))
    reading should be(1.0)

  it should "sense a robot at the exact edge of vertical alignment" in:
    val robotAtEdge = createRobot(Point2D(2, 1.0))
    val reading = getSensorReading(sensor, Set(robotAtEdge))
    reading should be(0.1)

  it should "sense a very small robot in front" in:
    val smallRobot = Robot(
      position = Point2D(2, 1),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value
    val reading = getSensorReading(sensor, Set(smallRobot))
    reading should be < 0.2 // Should detect the small robot close

  it should "sense a robot at the maximum range boundary" in:
    val robotAtMaxRange = createRobot(Point2D(6.49, 1))
    val reading = getSensorReading(sensor, Set(robotAtMaxRange))
    reading should be > 0.99 // Should detect the robot at the edge of range

  it should "sense an obstacle directly below" in:
    val obstacleBelow = createObstacle(Point2D(0.5, 2.0))
    val reading = getSensorReading(pointingDownSensor, Set(obstacleBelow))
    reading should be(0.0)

  it should "not sense an obstacle below outside its range" in:
    val farObstacleBelow = createObstacle(Point2D(0.5, 7.0))
    val reading = getSensorReading(pointingDownSensor, Set(farObstacleBelow))
    reading should be(1.0)

  it should "be able to sense a robot directly below" in:
    val robotBelow = createRobot(Point2D(0.5, 2.0))
    val reading = getSensorReading(pointingDownSensor, Set(robotBelow))
    reading should be(0.0)

  it should "not sense a robot below outside its range" in:
    val farRobotBelow = createRobot(Point2D(0.5, 7.0))
    val reading = getSensorReading(pointingDownSensor, Set(farRobotBelow))
    reading should be(1.0)

  it should "not sense an obstacle positioned slightly to the left relative to the downward sensor" in:
    val obstacleLeft = createObstacle(Point2D(1.01, 2.0), width = 0.5)
    val reading = getSensorReading(pointingDownSensor, Set(obstacleLeft))
    reading should be(1.0)

  it should "sense an obstacle at the exact horizontal alignment below" in:
    val obstacleAligned = createObstacle(Point2D(0.5, 2.0), height = 0.5)
    val reading = getSensorReading(pointingDownSensor, Set(obstacleAligned))
    reading should be < 0.1 // Should detect the obstacle directly below

  it should "sense a very thin obstacle below" in:
    val thinObstacle = createObstacle(Point2D(0.5, 2.0), width = 0.1, height = 1.0)
    val reading = getSensorReading(pointingDownSensor, Set(thinObstacle))
    reading should be(0.0)

  it should "sense an obstacle behind the robot" in:
    val obstacleBehind = createObstacle(Point2D(-0.5, 1.0))
    val reading = getSensorReading(pointingBackwardSensor, Set(obstacleBehind))
    reading should be(0.0)

  it should "not sense an obstacle behind outside its range" in:
    val farObstacleBehind = createObstacle(Point2D(-6.0, 1.0))
    val reading = getSensorReading(pointingBackwardSensor, Set(farObstacleBehind))
    reading should be(1.0)

  it should "be able to sense a robot behind" in:
    val robotBehind = createRobot(Point2D(-0.5, 1))
    val reading = getSensorReading(pointingBackwardSensor, Set(robotBehind))
    reading should be(0.0)

  it should "not sense a robot behind outside its range" in:
    val farRobotBehind = createRobot(Point2D(-6.0, 1))
    val reading = getSensorReading(pointingBackwardSensor, Set(farRobotBehind))
    reading should be(1.0)

  it should "sense an obstacle above the robot with left-pointing sensor" in:
    val obstacleAbove = createObstacle(Point2D(0.5, 0.0))
    val reading = getSensorReading(pointingLeftSensor, Set(obstacleAbove))
    reading should be(0.0)

  it should "not sense an obstacle above outside the left sensor's range" in:
    val farObstacleAbove = createObstacle(Point2D(0.5, -6.0))
    val reading = getSensorReading(pointingLeftSensor, Set(farObstacleAbove))
    reading should be(1.0)

  it should "be able to sense a robot above with left-pointing sensor" in:
    val robotAbove = createRobot(Point2D(0.5, 0.0))
    val reading = getSensorReading(pointingLeftSensor, Set(robotAbove))
    reading should be(0.0)

  it should "not sense a robot above outside the left sensor's range" in:
    val farRobotAbove = createRobot(Point2D(0.5, -6.0))
    val reading = getSensorReading(pointingLeftSensor, Set(farRobotAbove))
    reading should be(1.0)

  // Edge case tests for multiple entities
  it should "sense the closest obstacle when multiple obstacles are present" in:
    val closeObstacle = createObstacle(Point2D(1.5, 1.0))
    val farObstacle = createObstacle(Point2D(3.0, 1.0))
    val reading = getSensorReading(sensor, Set(closeObstacle, farObstacle))
    reading should be(0.0)

  it should "not sense anything when obstacles are positioned outside sensor path" in:
    val obstacleAbove = createObstacle(Point2D(2.0, 0.0))
    val obstacleBelow = createObstacle(Point2D(2.0, 2.0))
    val reading = getSensorReading(sensor, Set(obstacleAbove, obstacleBelow))
    reading should be(1.0)

  it should "sense mixed entities (robot and obstacle) with robot being closer" in:
    val closeRobot = createRobot(Point2D(1.5, 1.0))
    val farObstacle = createObstacle(Point2D(3.0, 1.0))
    val reading = getSensorReading(sensor, Set(closeRobot, farObstacle))
    reading should be(0.0)

  it should "sense mixed entities (robot and obstacle) with obstacle being closer" in:
    val closeObstacle = createObstacle(Point2D(1.5, 1.0))
    val farRobot = createRobot(Point2D(3.0, 1.0))
    val reading = getSensorReading(sensor, Set(closeObstacle, farRobot))
    reading should be(0.0)

  it should "handle empty environment correctly" in:
    val reading = getSensorReading(sensor, Set.empty)
    reading should be(1.0)

  // Diagonal sensor tests
  it should "sense an obstacle in the northeast diagonal" in:
    val obstacleNE =
      createObstacle(Point2D(1.5, 0.25), width = 1, height = 0.5) // Positioned along NE diagonal from sensor
    val environment = createEnvironment(Set(robot, obstacleNE))
    val sensorReading = pointingNorthEastSensor.sense(robot)(environment)
    sensorReading should be < 0.05 // Should detect obstacle very close

  it should "not sense an obstacle outside northeast sensor range" in:
    val farObstacleNE = createObstacle(Point2D(5.0, -4.0))
    val reading = getSensorReading(pointingNorthEastSensor, Set(farObstacleNE))
    reading should be(1.0)

  it should "sense a robot in the northeast diagonal" in:
    val robotNE = createRobot(Point2D(1.0, 0.5)) // Positioned along NE diagonal from sensor
    val environment = createEnvironment(Set(robot, robotNE))
    val sensorReading = pointingNorthEastSensor.sense(robot)(environment)
    sensorReading should be < 0.15 // Should detect robot very close

  it should "sense an obstacle in the southeast diagonal" in:
    val obstacleSE =
      createObstacle(Point2D(1.5, 1.5), width = 1, height = 0.5) // Positioned along SE diagonal from sensor
    val environment = createEnvironment(Set(robot, obstacleSE))
    val sensorReading = pointingSouthEastSensor.sense(robot)(environment)
    sensorReading should be < 0.05 // Should detect obstacle (return value less than 1.0)

  it should "not sense an obstacle outside southeast sensor range" in:
    val farObstacleSE = createObstacle(Point2D(5.0, 6.0))
    val reading = getSensorReading(pointingSouthEastSensor, Set(farObstacleSE))
    reading should be(1.0)

  // Tests with different robot orientations
  it should "sense correctly when robot is rotated 90 degrees" in:
    val rotatedRobot = createRobot(Point2D(0.5, 1.0), Orientation(90))
    val obstacleNorth = createObstacle(Point2D(0.5, 0.0)) // Place obstacle to the north
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleNorth))

    // Forward sensor (offset 0) should now point north due to 90-degree rotation
    val forwardSensorReading = sensor.sense(rotatedRobot)(envWithObstacle)
    forwardSensorReading should be(0.0) // Should detect obstacle to the north

  it should "sense correctly when robot is rotated 180 degrees" in:
    val rotatedRobot = createRobot(Point2D(0.5, 1.0), Orientation(180))
    val obstacleInFront = createObstacle(Point2D(1.5, 1.0))
    val obstacleBehind = createObstacle(Point2D(-0.5, 1.0))
    val envWithObstacles = createEnvironment(Set(rotatedRobot, obstacleInFront, obstacleBehind))

    // Forward sensor should now point backward due to 180-degree rotation
    val forwardSensorReading = sensor.sense(rotatedRobot)(envWithObstacles)
    forwardSensorReading should be(0.0) // Should detect obstacle that's now "in front"

  it should "sense correctly when robot is rotated 270 degrees" in:
    val rotatedRobot = createRobot(Point2D(0.5, 1.0), Orientation(270))
    val obstacleSouth = createObstacle(Point2D(0.5, 2.0)) // Place obstacle to the south
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleSouth))

    // Forward sensor should now point south due to 270-degree rotation
    val forwardSensorReading = sensor.sense(rotatedRobot)(envWithObstacle)
    forwardSensorReading should be(0.0) // Should detect obstacle to the south

  it should "sense correctly when robot is rotated 45 degrees" in:
    val rotatedRobot = createRobot(Point2D(0.5, 1.0), Orientation(45))
    val obstacleDiagonal = createObstacle(Point2D(1.5, 0.25), width = 1, height = 0.5) // Northeast diagonal
    val envWithObstacle = createEnvironment(Set(rotatedRobot, obstacleDiagonal))

    // Forward sensor should now point northeast due to 45-degree rotation
    val forwardSensorReading = sensor.sense(rotatedRobot)(envWithObstacle)
    forwardSensorReading should be < 0.05 // Should detect obstacle in the northeast direction

  it should "handle multiple obstacles at different angles with rotated robot" in:
    val rotatedRobot = createRobot(Point2D(5.0, 5.0), Orientation(90))
    val obstacleNorth = createObstacle(Point2D(5.0, 4.0))
    val obstacleEast = createObstacle(Point2D(6.0, 5.0))
    val obstacleSouth = createObstacle(Point2D(5.0, 6.0))
    val obstacleWest = createObstacle(Point2D(4.0, 5.0))

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
    val forwardReading = sensor.sense(rotatedRobot)(envWithObstacles)
    val backwardReading = pointingBackwardSensor.sense(rotatedRobot)(envWithObstacles)
    val leftReading = pointingLeftSensor.sense(rotatedRobot)(envWithObstacles)
    val downReading = pointingDownSensor.sense(rotatedRobot)(envWithObstacles)

    // All sensors should detect obstacles
    (forwardReading, backwardReading, leftReading, downReading) should be((0.0, 0.0, 0.0, 0.0))

  // Additional precision and boundary edge cases
  it should "handle floating point precision at range boundaries" in:
    val precisionObstacle = createObstacle(Point2D(6.4900001, 1.0))
    val reading = getSensorReading(sensor, Set(precisionObstacle))
    val _ = reading should be > 0.99
    reading should be < 1.0 // Should detect very close to range boundary

  it should "not sense obstacles at exactly distance + range boundary" in:
    val boundaryObstacle = createObstacle(Point2D(6.5000001, 1.0))
    val reading = getSensorReading(sensor, Set(boundaryObstacle))
    reading should be(1.0)

  it should "handle very small robots at edge of detection" in:
    val tinyRobot = Robot(
      position = Point2D(6.49, 1.0),
      shape = ShapeType.Circle(0.01),
      orientation = Orientation(0.0),
      actuators = Seq.empty[Actuator[Robot]],
      sensors = SensorSuite.empty,
    ).toOption.value
    val reading = getSensorReading(sensor, Set(tinyRobot))
    reading should be > 0.99

  it should "handle obstacles with extreme aspect ratios" in:
    val tallThinObstacle = createObstacle(Point2D(2.0, 1.0), width = 0.001, height = 10.0)
    val environment = createEnvironment(Set(robot, tallThinObstacle))
    val sensorReading = sensor.sense(robot)(environment)
    sensorReading should be < 1.0 // Should detect very thin obstacle

end ProximitySensorTest
