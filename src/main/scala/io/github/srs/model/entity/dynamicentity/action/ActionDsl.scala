package io.github.srs.model.entity.dynamicentity.action

import cats.Monad

/**
 * ActionDsl provides an extension method for chaining actions in a DSL style.
 */
object ActionDsl:

  extension [F[_]: Monad](a: Action[F])
    /**
     * Chains the current action with the next action in a sequence.
     * @param next
     *   the next action to be executed after the current action.
     * @return
     *   the combined action as a [[SequenceAction]].
     */
    infix def thenDo(next: Action[F]): Action[F] = SequenceAction(List(a, next))

    /**
     * Chains the current action with a list of actions in a sequence.
     * @param next
     *   the list of actions to be executed after the current action.
     * @return
     *   the combined action as a [[SequenceAction]].
     */
    infix def thenDo(next: List[Action[F]]): Action[F] = SequenceAction(a :: next)
