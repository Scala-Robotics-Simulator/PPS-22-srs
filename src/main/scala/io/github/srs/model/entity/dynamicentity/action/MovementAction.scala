package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * MovementAction represents a dynamic-entity movement action characterized by the speeds applied to the left and right
 * wheels.
 * @param leftSpeed
 *   the speed to apply to the left wheel.
 * @param rightSpeed
 *   the speed to apply to the right wheel.
 * @tparam F
 *   the effect type of the action.
 * @tparam E
 *   the type of dynamic entity on which the action is performed, extending DynamicEntity.
 */
final case class MovementAction[F[_]] private[action] (leftSpeed: Double, rightSpeed: Double) extends Action[F]:

  /**
   * Runs the movement action using the provided [[ActionAlg]].
   *
   * @param a
   *   the [[ActionAlg]] to use for executing the movement action.
   * @return
   *   an effectful computation that results in the dynamic entity after the wheels have been moved.
   */
  override def run[E <: DynamicEntity](dynamicEntity: E)(using a: ActionAlg[F, E]): F[E] =
    a.moveWheels(dynamicEntity, leftSpeed, rightSpeed)
