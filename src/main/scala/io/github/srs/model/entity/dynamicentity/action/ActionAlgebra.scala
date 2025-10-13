package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * ActionAlg trait defines the actions that can be performed on a dynamic entity.
 *
 * It represents the algebra of actions that can be executed on a dynamic entity, such as, for example, moving the
 * wheels.
 * @tparam F
 *   the effect type of the action.
 * @tparam E
 *   the type of dynamic entity on which the action is performed, extending [[DynamicEntity]].
 */
trait ActionAlgebra[F[_], E <: DynamicEntity]:
  /**
   * Moves the dynamic entity wheels with the specified speeds.
   * @param e
   *   the dynamic entity on which the action will be executed.
   * @param leftSpeed
   *   the speed to apply to the left wheel.
   * @param rightSpeed
   *   the speed to apply to the right wheel.
   * @return
   *   an effectful computation that results in the dynamic entity after the wheels have been moved.
   */
  def moveWheels(e: E, leftSpeed: Double, rightSpeed: Double): F[E]
