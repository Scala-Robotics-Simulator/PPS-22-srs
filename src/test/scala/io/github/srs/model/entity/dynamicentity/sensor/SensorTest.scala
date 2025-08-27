package io.github.srs.model.entity.dynamicentity.sensor

import java.util.UUID

import cats.Monad
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import cats.Id

class SensorTest extends AnyFlatSpec with should.Matchers:
  given CanEqual[Sensor[?, ?], Sensor[?, ?]] = CanEqual.derived

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val offset: Orientation = Orientation(0.0)
  val range: Range = 10.0

  class Dummy(
      override val id: UUID = UUID.randomUUID(),
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[Actuator[Dummy]],
      override val sensors: Vector[Sensor[Dummy, Environment]],
      override val behavior: Policy = Policy.AlwaysForward,
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  class DummySensor(override val offset: Orientation) extends Sensor[Dummy, Environment]:
    override type Data = Double

    override def sense[F[_]: Monad](entity: Dummy, env: Environment): F[Double] =
      Monad[F].pure(42.0) // Dummy implementation for sensing

  "Sensor" should "have an offset orientation" in:
    val sensor = new DummySensor(offset)
    sensor.offset should be(offset)

  it should "sense the environment and return data" in:
    val sensor = new DummySensor(offset)
    val entity = new Dummy(
      position = initialPosition,
      shape = shape,
      orientation = initialOrientation,
      actuators = Seq.empty[Actuator[Dummy]],
      sensors = Vector(sensor),
    )
    val environment = Environment(10, 10).validate.toOption.value
    val data = sensor.sense[Id](entity, environment)
    data should be(42.0)

end SensorTest
