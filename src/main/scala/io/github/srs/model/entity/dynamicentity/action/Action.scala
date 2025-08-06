package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * Action trait represents an action that can be performed on a dynamic entity.
 * @tparam F
 *   the effect type of the action.
 * @tparam E
 *   the type of dynamic entity on which the action is performed, extending DynamicEntity.
 */
trait Action[F[_], E <: DynamicEntity]:
  /**
   * Runs the action using the provided [[ActionAlg]].
   * @param e
   *   the dynamic entity on which the action will be executed.
   * @param a
   *   the [[ActionAlg]] to use for executing the action.
   * @return
   *   an effectful computation that results in the dynamic entity after the action is executed.
   */
  def run(e: E)(using a: ActionAlg[F, E]): F[E]
