package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*

/**
 * A [[Behavior]] that always makes the entity move forward.
 */
object AlwaysForwardBehavior:

  /**
   * The decision function for the always forward behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[Decision]] that always returns a forward movement action.
   */
  def decision[F[_]]: Decision[F] =
    Kleisli(ctx => (forward[F], ctx.rng))
