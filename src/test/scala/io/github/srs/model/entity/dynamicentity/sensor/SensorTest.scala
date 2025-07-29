package io.github.srs.model.entity.dynamicentity.sensor

import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class SensorTest extends AnyFlatSpec with should.Matchers:
  given CanEqual[Sensor[?, ?], Sensor[?, ?]] = CanEqual.derived

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val offset: Orientation = Orientation(0.0)
  val distance: Distance = 1.0
  val range: Range = 10.0

  class Dummy(
      val position: Point2D,
      val shape: ShapeType,
      val orientation: Orientation,
      val actuators: Seq[Actuator[Dummy]],
      val sensors: Vector[Sensor[Dummy, Environment]],
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  class DummySensor(override val offset: Orientation, override val distance: Distance, override val range: Range)
      extends Sensor[Dummy, Environment]:
    override type Data = Double

    override def sense[F[_]](entity: Dummy, env: Environment)(using Monad[F]): F[Double] =
      Monad[F].pure(42.0) // Dummy implementation for sensing

  "Sensor" should "have an offset orientation, distance, and range" in:
    val sensor = new DummySensor(offset, distance, range)
    (sensor.offset, sensor.distance, sensor.range) should be((offset, distance, range))

  it should "sense the environment and return data" in:
    val sensor = new DummySensor(offset, distance, range)
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, Vector(sensor))
    val environment = Environment(10, 10).validate.toOption.value
    val data = sensor.sense[Id](entity, environment)
    data should be(42.0)

end SensorTest
