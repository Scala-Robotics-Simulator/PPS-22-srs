package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.validation.Validation

/**
 * Represents an actuator for a dynamic entity.
 *
 * @tparam E
 *   the type of dynamic entity that the actuator can act upon.
 */
trait Actuator[E <: DynamicEntity]:
  /**
   * Applies an action to the given entity, returning a new instance with the updated state.
   *
   * This method is intended to modify the state of the entity based on the actuator's logic.
   *
   * @param entity
   *   the entity to act upon.
   * @return
   *   a new instance of the entity with the updated state, wrapped in a `Validation` to handle any potential errors.
   */
  def act(entity: E): Validation[E]
