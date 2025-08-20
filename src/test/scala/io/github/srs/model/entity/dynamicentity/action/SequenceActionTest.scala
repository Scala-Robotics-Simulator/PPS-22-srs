package io.github.srs.model.entity.dynamicentity.action

import java.util.UUID

import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.{ moveForward, turnRight }
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.behavior.Behaviors
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReadings }
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
      override val id: UUID = UUID.randomUUID(),
      override val position: Point2D,
      override val shape: ShapeType,
      override val orientation: Orientation,
      override val actuators: Seq[DummyActuator],
      override val sensors: Vector[Sensor[Dummy, Environment]],
      override val behavior: Behavior[SensorReadings, Action[IO]] = Behaviors.simple[IO],
  ) extends DynamicEntity:
    def act[F[_]: Monad](): F[Dummy] = Monad[F].pure(this)

  val dynamicEntity: Dummy = Dummy(
    position = Point2D(0, 0),
    shape = ShapeType.Circle(1.0),
    orientation = Orientation(0),
    actuators = Seq(DummyActuator()),
    sensors = Vector.empty[Sensor[Dummy, Environment]],
  )

  "SequenceAction" should "execute multiple actions in sequence" in:
    val action1: Action[Id] = NoAction[Id]()
    val action2: Action[Id] = moveForward[Id]
    val action3: Action[Id] = turnRight[Id]
    val sequenceAction: SequenceAction[Id] = SequenceAction(List(action1, action2, action3))
    val updatedEntity: Dummy = sequenceAction.run(dynamicEntity)
    val _ = dynamicEntity.position should be(updatedEntity.position)
    val _ = dynamicEntity.shape should be(updatedEntity.shape)
    val _ = dynamicEntity.orientation should be(updatedEntity.orientation)
    val _ = dynamicEntity.actuators should be(updatedEntity.actuators)
    dynamicEntity.sensors should be(updatedEntity.sensors)

  it should "handle an empty sequence of actions" in:
    val emptySequenceAction: SequenceAction[Id] = SequenceAction(List.empty)
    val updatedEntity: Dummy = emptySequenceAction.run(dynamicEntity)
    dynamicEntity should be(updatedEntity)
end SequenceActionTest
