package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * The ActionAlgebra trait defines the algebra of actions that can be performed on a dynamic entity, particularly
 * actions that involve interacting with or modifying the entity's state.
 *
 * @tparam F
 *   the effectful computation type in which the actions will be performed.
 * @tparam E
 *   the type of dynamic entity on which the actions will be performed, extending DynamicEntity.
 */
trait ActionAlgebra[F[_], E <: DynamicEntity]:

  /**
   * Moves the wheels of the given dynamic entity by applying specified speeds to the left and right wheels.
   *
   * @param e
   *   the dynamic entity whose wheels are to be moved.
   * @param leftSpeed
   *   the speed to apply to the left wheel.
   * @param rightSpeed
   *   the speed to apply to the right wheel.
   * @return
   *   an effectful computation resulting in the dynamic entity after the movement action is applied.
   */
  def moveWheels(e: E, leftSpeed: Double, rightSpeed: Double): F[E]
