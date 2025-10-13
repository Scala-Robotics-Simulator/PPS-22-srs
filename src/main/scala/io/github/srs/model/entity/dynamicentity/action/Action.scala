package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * Action trait represents an action that can be performed on a dynamic entity.
 * @tparam F
 *   the effect type of the action.
 */
trait Action[F[_]]:
  /**
   * Runs the action using the provided [[ActionAlgebra]].
   *
   * @param e
   *   the dynamic entity on which the action will be executed.
   * @param a
   *   the [[ActionAlgebra]] to use for executing the action.
   * @tparam E
   *   the type of the dynamic entity on which the action will be executed.
   * @return
   *   an effectful computation that results in the dynamic entity after the action is executed.
   */
  def run[E <: DynamicEntity](e: E)(using a: ActionAlgebra[F, E]): F[E]
