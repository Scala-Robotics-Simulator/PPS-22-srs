package io.github.srs.model.entity.dynamicentity.action

import cats.Monad
import cats.syntax.foldable.toFoldableOps
import io.github.srs.model.entity.dynamicentity.DynamicEntity

/**
 * SequenceAction represents a composite action that executes a sequence of actions in order.
 *
 * @param actions
 *   the list of actions to be executed in sequence.
 * @param monad$F$0
 *   the implicit Monad instance for the effect type F.
 * @tparam F
 *   the effect type of the action.
 * @tparam E
 *   the type of dynamic entity on which the action will be executed, extending DynamicEntity.
 */
final case class SequenceAction[F[_]: Monad](actions: List[Action[F]]) extends Action[F]:

  /**
   * Runs the sequence of actions on the given dynamic entity.
   *
   * @param dynamicEntity
   *   the dynamic entity on which the actions will be executed.
   * @param a
   *   the [[ActionAlg]] to use for executing the actions.
   * @return
   *   an effectful computation that results in the dynamic entity after all actions has been executed.
   */
  override def run[E <: DynamicEntity](dynamicEntity: E)(using a: ActionAlg[F, E]): F[E] =
    actions.foldLeftM(dynamicEntity)((e, act) => act.run(e))

object SequenceAction:

  extension [F[_]: Monad, E <: DynamicEntity, A <: Action[F]](a: A)
    /**
     * Chains the current action with the next action in a sequence.
     *
     * @param next
     *   the next action to be executed after the current action.
     * @return
     *   the combined action as a [[SequenceAction]].
     */
    infix def thenDo(next: A): Action[F] = SequenceAction(List(a, next))
