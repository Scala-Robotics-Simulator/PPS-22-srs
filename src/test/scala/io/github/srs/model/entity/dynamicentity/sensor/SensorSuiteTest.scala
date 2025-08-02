package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity, Robot }
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.model.environment.dsl.CreationDSL.*

class SensorSuiteTest extends AnyFlatSpec with Matchers:
  given CanEqual[ProximitySensor[?, ?], ProximitySensor[?, ?]] = CanEqual.derived

  // Test sensor configurations
  val frontSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(0.0)
  val backSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(180.0)
  val leftSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(90.0)
  val rightSensor: ProximitySensor[DynamicEntity, Environment] = createSensor(270.0)

  // Test robot
  val robot: Robot = createRobot(Point2D(5.0, 5.0))

  private def createSensor(orientationDegrees: Double): ProximitySensor[DynamicEntity, Environment] =
    ProximitySensor(Orientation(orientationDegrees), 0.5, 3.0).toOption.value

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
      width = 20,
      height = 20,
      entities = entities,
    ).validate.toOption.value

  "SensorSuite" should "be created with no sensors (empty)" in:
    val emptySuite = SensorSuite.empty
    emptySuite.proximitySensors should be(Vector.empty)

  it should "be created with a single proximity sensor" in:
    val suite = SensorSuite(frontSensor)
    suite.proximitySensors should contain only frontSensor

  it should "be created with multiple proximity sensors" in:
    val suite = SensorSuite(frontSensor, backSensor, leftSensor, rightSensor)
    suite.proximitySensors should contain allOf (frontSensor, backSensor, leftSensor, rightSensor)

  it should "preserve the order of sensors" in:
    val suite = SensorSuite(rightSensor, leftSensor, frontSensor, backSensor)
    suite.proximitySensors should be(Vector(rightSensor, leftSensor, frontSensor, backSensor))

  it should "return empty sensor readings when no sensors are present" in:
    val emptySuite = SensorSuite.empty
    val environment = createEnvironment(Set(robot))
    val readings = emptySuite.sense(robot, environment)

    readings.proximity should be(Vector.empty)

  it should "return sensor readings for multiple sensors with no obstacles" in:
    val suite = SensorSuite(frontSensor, backSensor, leftSensor, rightSensor)
    val environment = createEnvironment(Set(robot))
    val readings = suite.sense(robot, environment)

    val _ = readings.proximity should have size 4
    readings.proximity.foreach(_.value should be(1.0)) // No obstacles detected

  it should "maintain sensor order in readings" in:
    val suite = SensorSuite(rightSensor, leftSensor, frontSensor, backSensor)
    val environment = createEnvironment(Set(robot))
    val readings = suite.sense(robot, environment)

    val _ = readings.proximity should have size 4
    // Check that sensors appear in the same order as they were added
    readings.proximity.map(_.sensor) should be(Vector(rightSensor, leftSensor, frontSensor, backSensor))

  it should "create SensorSuite using apply method with varargs" in:
    val suite1 = SensorSuite()
    val suite2 = SensorSuite(frontSensor)
    val suite3 = SensorSuite(frontSensor, backSensor)
    val suite4 = SensorSuite(frontSensor, backSensor, leftSensor, rightSensor)

    val _ = suite1.proximitySensors should be(Vector.empty)
    val _ = suite2.proximitySensors should be(Vector(frontSensor))
    val _ = suite3.proximitySensors should be(Vector(frontSensor, backSensor))
    suite4.proximitySensors should be(Vector(frontSensor, backSensor, leftSensor, rightSensor))

  it should "have working empty factory method" in:
    val emptySuite1 = SensorSuite.empty
    val emptySuite2 = SensorSuite()

    val _ = emptySuite1.proximitySensors should be(emptySuite2.proximitySensors)
    emptySuite1.proximitySensors should be(Vector.empty)

end SensorSuiteTest
