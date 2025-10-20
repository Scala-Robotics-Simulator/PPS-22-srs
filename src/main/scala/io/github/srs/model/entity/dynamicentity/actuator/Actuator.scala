package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * Represents an actuator for a dynamic entity.
 *
 * @tparam E
 *   the type of dynamic entity that the actuator can act upon.
 */
trait Actuator[E <: DynamicEntity]:


  /**
   * Apply the actuator effect for a given time delta.
   *
   * @param dt     the timestep for which the actuation is applied
   * @param entity the entity being actuated
   * @param kin    typeclass providing pose-update semantics
   */
  def act[F[_]: Monad](dt: FiniteDuration, entity: E)(using kin: Kinematics[E]): F[E]
