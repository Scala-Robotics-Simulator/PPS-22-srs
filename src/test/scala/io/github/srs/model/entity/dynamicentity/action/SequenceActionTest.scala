package io.github.srs.model.entity.dynamicentity.action

import scala.concurrent.duration.FiniteDuration

import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.moveForward
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.{ Actuator, DynamicEntity }
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SequenceActionTest extends AnyFlatSpec with Matchers:

  given CanEqual[Dummy, Dummy] = CanEqual.derived

  given actionAlg: ActionAlg[Id, Dummy] with

    def moveWheels(entity: Dummy, left: Double, right: Double): Dummy =
      val updatedActuators: Seq[DummyActuator] = Seq(DummyActuator())
      Dummy(
        position = entity.position,
        shape = entity.shape,
        orientation = entity.orientation,
        actuators = updatedActuators,
        sensors = entity.sensors,
      )

  case class DummyActuator() extends Actuator[Dummy]:
    override def act[F[_]: Monad](dt: FiniteDuration, entity: Dummy): F[Dummy] = Monad[F].pure(entity)

  case class Dummy(
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[DummyActuator],
      override val sensors: Vector[Sensor[Dummy, Environment]],
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  val dynamicEntity = new Dummy(
    position = Point2D(0, 0),
    shape = ShapeType.Circle(1.0),
    orientation = Orientation(0),
    actuators = Seq(DummyActuator()),
    sensors = Vector.empty[Sensor[Dummy, Environment]],
  )

  "SequenceAction" should "execute multiple actions in sequence" in:
    val action1: Action[Id, Dummy] = NoAction[Id, Dummy]()
    val action2: Action[Id, Dummy] = moveForward[Id, Dummy]
    val sequenceAction: SequenceAction[Id, Dummy] = SequenceAction(List(action1, action2))
    val updatedEntity: Dummy = sequenceAction.run(dynamicEntity)
    dynamicEntity should be(updatedEntity)

  it should "handle an empty sequence of actions" in:
    val emptySequenceAction: SequenceAction[Id, Dummy] = SequenceAction(List.empty)
    val updatedEntity: Dummy = emptySequenceAction.run(dynamicEntity)
    dynamicEntity should be(updatedEntity)
end SequenceActionTest
