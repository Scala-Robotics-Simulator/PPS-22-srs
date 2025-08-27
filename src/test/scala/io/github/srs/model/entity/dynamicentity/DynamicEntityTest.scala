package io.github.srs.model.entity.dynamicentity

import java.util.UUID

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DynamicEntityTest extends AnyFlatSpec with Matchers:

  val initialPosition: Point2D = Point2D(0.0, 0.0)
  val initialOrientation: Orientation = Orientation(0.0)
  val shape: ShapeType.Circle = ShapeType.Circle(0.5)

  val sensorOffset: Orientation = Orientation(0.0)
  val sensorRange: Double = 1.0

  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(
    offset = sensorOffset,
    range = sensorRange,
  )

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

  class DummyActuator extends Actuator[Dummy]:
    override def act[F[_]: Monad](dt: FiniteDuration, entity: Dummy): F[Dummy] = Monad[F].pure(entity)

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(
      position = initialPosition,
      shape = shape,
      orientation = initialOrientation,
      actuators = Seq.empty[Actuator[Dummy]],
      sensors = Vector.empty[Sensor[Dummy, Environment]],
    )
    entity.actuators should be(Seq.empty)

  it should "support having some actuators" in:
    val actuator = new DummyActuator()
    val entity = new Dummy(
      position = initialPosition,
      shape = shape,
      orientation = initialOrientation,
      actuators = Seq(actuator),
      sensors = Vector.empty[Sensor[Dummy, Environment]],
    )
    entity.actuators should be(Seq(actuator))

  it should "support having no sensors" in:
    val entity = new Dummy(
      position = initialPosition,
      shape = shape,
      orientation = initialOrientation,
      actuators = Seq.empty[Actuator[Dummy]],
      sensors = Vector.empty[Sensor[Dummy, Environment]],
    )
    entity.sensors should be(Vector.empty)

  it should "support having some sensors" in:
    val entityWithSensors =
      new Dummy(
        position = initialPosition,
        shape = shape,
        orientation = initialOrientation,
        actuators = Seq.empty[Actuator[Dummy]],
        sensors = Vector(sensor),
      )
    entityWithSensors.sensors should be(Vector(sensor))

end DynamicEntityTest
