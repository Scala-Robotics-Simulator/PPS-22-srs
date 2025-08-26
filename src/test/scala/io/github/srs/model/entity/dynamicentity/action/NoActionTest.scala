package io.github.srs.model.entity.dynamicentity.action

import java.util.UUID

import scala.concurrent.duration.FiniteDuration

import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NoActionTest extends AnyFlatSpec with Matchers:

  class DummyActuator extends Actuator[Dummy]:
    override def act[F[_]: Monad](dt: FiniteDuration, entity: Dummy): F[Dummy] = Monad[F].pure(entity)

  class Dummy(
      override val id: UUID = UUID.randomUUID(),
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[DummyActuator],
      override val sensors: Vector[Sensor[Dummy, Environment]],
      override val behavior: Policy = Policy.AlwaysForward,
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  given CanEqual[Dummy, Dummy] = CanEqual.derived

  given actionAlg: ActionAlg[Id, Dummy] with

    def moveWheels(entity: Dummy, left: Double, right: Double): Dummy =
      val updatedActuators: Seq[DummyActuator] = Seq(DummyActuator())
      new Dummy(
        position = entity.position,
        shape = entity.shape,
        orientation = entity.orientation,
        actuators = updatedActuators,
        sensors = entity.sensors,
      )

  "NoAction" should "do nothing on the dynamic entity" in:
    val dynamicEntity = new Dummy(
      position = Point2D(0, 0),
      shape = ShapeType.Circle(1.0),
      orientation = Orientation(0),
      actuators = Seq(DummyActuator()),
      sensors = Vector.empty[Sensor[Dummy, Environment]],
    )
    val noAction: NoAction[Id] = NoAction[Id]()
    val updateEntity: Dummy = noAction.run(dynamicEntity)(using actionAlg)
    dynamicEntity should be(updateEntity)
end NoActionTest
