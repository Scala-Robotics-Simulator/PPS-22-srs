package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.validation.Validation

/**
 * Represents an actuator for a dynamic entity.
 *
 * @tparam E
 *   the type of dynamic entity that the actuator can act upon.
 */
trait Actuator[E <: DynamicEntity]:

  /**
   * Performs an action on the dynamic entity based on the specified time duration.
   *
   * @param dt
   *   the time duration for which the action is performed.
   * @param entity
   *   the dynamic entity to act upon.
   * @return
   *   a validation result containing the updated entity after the action is applied.
   */
  def act(dt: FiniteDuration, entity: E): Validation[E]
