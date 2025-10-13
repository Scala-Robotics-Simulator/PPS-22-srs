package io.github.srs.model.entity.dynamicentity.action

import cats.Monad
import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * NoAction represents an action that does nothing on the dynamic entity.
 * @tparam F
 *   the effect type of the action.
 */
final case class NoAction[F[_]: Monad]() extends Action[F]:

  /**
   * Runs the no-action on the given dynamic entity.
   * @param dynamicEntity
   *   the dynamic entity on which the action will be executed.
   * @param a
   *   the ActionAlg to use for executing the action.
   * @return
   *   a new instance of [[DynamicEntity]] after executing the action (which is the same as the input entity).
   */
  override def run[E <: DynamicEntity](dynamicEntity: E)(using a: ActionAlgebra[F, E]): F[E] =
    Monad[F].pure(dynamicEntity)
