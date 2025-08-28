package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*

object AlwaysForwardBehavior:

  def decision[F[_]]: Decision[F] =
    Kleisli(ctx => (MovementActionFactory.moveForward[F], ctx.rng))
