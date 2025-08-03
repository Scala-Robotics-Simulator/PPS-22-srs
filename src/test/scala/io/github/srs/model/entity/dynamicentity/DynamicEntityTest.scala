package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.{ DomainError, Validation }
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DynamicEntityTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val sensorOffset: Orientation = Orientation(0.0)
  val sensorDistance: Double = 0.5
  val sensorRange: Double = 1.0

  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(
    offset = sensorOffset,
    distance = sensorDistance,
    range = sensorRange,
  ).toOption.value

  class Dummy(
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[Actuator[Dummy]],
      override val sensors: SensorSuite,
  ) extends DynamicEntity:
    def act(): Validation[Dummy] = Right[DomainError, Dummy](this)

  class DummyActuator extends Actuator[Dummy]:
    override def act(dt: FiniteDuration, entity: Dummy): Validation[Dummy] = Right[DomainError, Dummy](entity)

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, SensorSuite.empty)
    entity.actuators should be(Seq.empty)

  it should "support having some actuators" in:
    val actuator = new DummyActuator()
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq(actuator), SensorSuite.empty)
    entity.actuators should be(Seq(actuator))

  it should "support having no sensors" in:
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, SensorSuite.empty)
    entity.sensors.proximitySensors should be(Vector.empty)

  it should "support having some sensors" in:
    val entityWithSensors =
      new Dummy(initialPosition, shape, initialOrientation, Seq.empty, SensorSuite(sensor))
    entityWithSensors.sensors.proximitySensors should be(Vector(sensor))

end DynamicEntityTest
