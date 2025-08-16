package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Rule
import io.github.srs.model.entity.dynamicentity.behavior.Rules
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
  val sensorDistance: Double = 0.5
  val sensorRange: Double = 1.0

  val sensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor(
    offset = sensorOffset,
    distance = sensorDistance,
    range = sensorRange,
  )

  class Dummy(
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[Actuator[Dummy]],
      override val sensors: Vector[Sensor[Dummy, Environment]],
      override val behavior: Rule[IO, SensorReadings, Action[IO]] = Rules.alwaysForward,
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  class DummyActuator extends Actuator[Dummy]:
    override def act[F[_]: Monad](dt: FiniteDuration, entity: Dummy): F[Dummy] = Monad[F].pure(entity)

  "DynamicEntity" should "support having no actuators" in:
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, Vector.empty)
    entity.actuators should be(Seq.empty)

  it should "support having some actuators" in:
    val actuator = new DummyActuator()
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq(actuator), Vector.empty)
    entity.actuators should be(Seq(actuator))

  it should "support having no sensors" in:
    val entity = new Dummy(initialPosition, shape, initialOrientation, Seq.empty, Vector.empty)
    entity.sensors should be(Vector.empty)

  it should "support having some sensors" in:
    val entityWithSensors =
      new Dummy(initialPosition, shape, initialOrientation, Seq.empty, Vector(sensor))
    entityWithSensors.sensors should be(Vector(sensor))

end DynamicEntityTest
