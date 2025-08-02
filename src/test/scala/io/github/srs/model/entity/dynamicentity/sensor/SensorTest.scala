package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.PositiveDouble
import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity }
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.{ DomainError, Validation }
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import io.github.srs.model.environment.dsl.CreationDSL.*

class SensorTest extends AnyFlatSpec with should.Matchers:
  given CanEqual[Sensor[?, ?, ?], Sensor[?, ?, ?]] = CanEqual.derived

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val offset: Orientation = Orientation(0.0)
  val distance: Distance = PositiveDouble(1.0).toOption.value
  val range: Range = PositiveDouble(10.0).toOption.value

  class Dummy(
      val position: Point2D,
      val shape: ShapeType,
      val orientation: Orientation,
      val actuators: Seq[Actuator[Dummy]],
      val sensors: SensorSuite,
  ) extends DynamicEntity:
    def act(): Validation[Dummy] = Right[DomainError, Dummy](this)

  class DummySensor(override val offset: Orientation, override val distance: Distance, override val range: Range)
      extends Sensor[DynamicEntity, Environment, Double]:

    override def sense(entity: DynamicEntity)(environment: Environment): Double =
      // Dummy implementation for testing purposes
      42.0

  "Sensor" should "have an offset orientation, distance, and range" in:
    val sensor = new DummySensor(offset, distance, range)
    (sensor.offset, sensor.distance, sensor.range) should be((offset, distance, range))

  it should "sense the environment and return data" in:
    val sensor = new DummySensor(offset, distance, range)
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, SensorSuite.empty)
    val environment = Environment(10, 10).validate.toOption.value
    val data = sensor.sense(entity)(environment)
    data should be(42.0)

end SensorTest
