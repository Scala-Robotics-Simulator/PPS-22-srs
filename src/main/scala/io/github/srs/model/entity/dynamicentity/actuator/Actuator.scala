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
   * Performs an action on the entity after a specified duration.
   *
   * @param dt
   *   the duration after which the action is performed.
   * @param entity
   *   the dynamic entity on which the action is performed.
   * @tparam F
   *   the effect type in which the action is performed, a Monad.
   * @return
   *   the updated entity after the action is performed.
   */
  def act[F[_]: Monad](dt: FiniteDuration, entity: E): F[E]
