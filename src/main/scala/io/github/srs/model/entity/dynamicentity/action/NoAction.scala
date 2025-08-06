package io.github.srs.model.entity.dynamicentity.action

import cats.Monad
import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * NoAction represents an action that does nothing on the dynamic entity.
 * @param monad$F$0
 *   the implicit Monad instance for the effect type F.
 * @tparam F
 *   the effect type of the action.
 * @tparam E
 *   the type of the dynamic entity on which the action will be executed.
 */
final case class NoAction[F[_]: Monad, E <: DynamicEntity]() extends Action[F, E]:

  /**
   * Runs the no-action on the given dynamic entity.
   * @param dynamicEntity
   *   the dynamic entity on which the action will be executed.
   * @param a
   *   the ActionAlg to use for executing the action.
   * @return
   *   a new instance of [[DynamicEntity]] after executing the action (which is the same as the input entity).
   */
  override def run(dynamicEntity: E)(using a: ActionAlg[F, E]): F[E] =
    Monad[F].pure(dynamicEntity)
