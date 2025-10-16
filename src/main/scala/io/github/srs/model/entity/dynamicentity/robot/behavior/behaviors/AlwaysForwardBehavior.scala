package io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors

import cats.Id
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext

import BehaviorCommon.*

/**
 * A [[io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior]] that always makes the entity move
 * forward.
 */
object AlwaysForwardBehavior:

  /**
   * The decision function for the always forward behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[BehaviorCommon.Decision]] that always returns a forward movement action.
   */
  def decision[F[_]]: Decision[F] =
    Kleisli.ask[Id, BehaviorContext].map { ctx =>
      (forward[F], ctx.rng)
    }
