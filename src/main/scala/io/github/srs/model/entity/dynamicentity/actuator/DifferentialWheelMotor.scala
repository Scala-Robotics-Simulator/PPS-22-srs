package io.github.srs.model.entity.dynamicentity.actuator

import cats.Monad
import cats.syntax.flatMap.*
import cats.syntax.foldable.*
import cats.syntax.functor.*
import io.github.srs.model.entity.*
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.{Action, ActionAlgebra, SequenceAction}
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor.DefaultWheel

import scala.concurrent.duration.FiniteDuration

/**
 * A differential wheel motor actuator for dynamic entities.
 */
trait DifferentialWheelMotor[E <: DynamicEntity] extends Actuator[E]:
  def left: Wheel
  def right: Wheel

object DifferentialWheelMotor:

  /** Factory */
  def apply[E <: DynamicEntity](
      left: Wheel = DefaultWheel,
      right: Wheel = DefaultWheel,
  ): DifferentialWheelMotor[E] =
    DifferentialWheelMotorImpl(left, right)

  /** Concrete implementation */
  private case class DifferentialWheelMotorImpl[E <: DynamicEntity](
      left: Wheel,
      right: Wheel,
  ) extends DifferentialWheelMotor[E]:

    override def act[F[_]: Monad](
        dt: FiniteDuration,
        entity: E,
    )(using kin: Kinematics[E]): F[E] =
      import DifferentialKinematics.*

      val wheelDistance = kin.radius(entity) * 2
      val theta = kin.orientation(entity).toRadians

      val (dx, dy, newOrientation) =
        (computeWheelVelocities
          andThen computeVelocities(wheelDistance)
          andThen computePositionAndOrientation(theta, dt))(this)

      val newPos = Point2D(kin.position(entity).x + dx, kin.position(entity).y + dy)
      Monad[F].pure(kin.withPose(entity, newPos, newOrientation))

  end DifferentialWheelMotorImpl

  extension [E <: DynamicEntity](motor: DifferentialWheelMotor[E])

    /** Applies kinematics using this specific motor */
    def applyTo[F[_]: Monad](entity: E, dt: FiniteDuration)(using kin: Kinematics[E]): F[E] =
      motor.act(dt, entity)

    /** Action + movement logic (no iteration through actuators, motor is already known) */
    def applyMovementActions[F[_]: Monad](
        entity: E,
        dt: FiniteDuration,
        action: Action[F],
    )(using a: ActionAlgebra[F, E], kin: Kinematics[E]): F[E] =
      action match
        case seq: SequenceAction[F] =>
          seq.actions.foldLeftM(entity)((e, step) => motor.applyMovementActions(e, dt, step))
        case single =>
          for
            withSpeeds <- single.run(entity)
            moved <- motor.applyTo(withSpeeds, dt)
          yield moved
  end extension
end DifferentialWheelMotor
