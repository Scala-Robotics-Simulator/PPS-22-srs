package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Monad
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.action.{ MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.utils.SimulationDefaults.Behaviors.RandomWalk.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }

/**
 * A behavior that makes random movement decisions.
 *
 * It randomly selects speeds for the left and right wheels, with a bias towards forward movement. Occasionally, it may
 * perform a pivot turn to change direction more sharply.
 */
object RandomWalkBehavior:

  def decision[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      import io.github.srs.utils.random.RandomDSL.*
      val range = MinSpeed to MaxSpeed
      val (uF, r1) = ctx.rng generate range.includeMax
      val (uT, r2) = r1 generate range.includeMax
      val (uM, r3) = r2 generate range.includeMax

      val base =
        clamp(
          MaxSpeed * (MinForwardFactor + MaxForwardExtra * math.abs(uF)),
          MinSpeed,
          MaxSpeed,
        )

      val minTurn = base * MinTurnOfBase
      val maxTurn = math.min(MaxSpeed, base * MaxTurnOfBase)
      val turnCore =
        minTurn + (maxTurn - minTurn) * math.pow(math.abs(uT), TurnExponent)

      val span = MaxSpeed - MinSpeed
      val uM01 = if span == 0.0 then 0.5 else (uM - MinSpeed) / span
      val pivotBoost =
        if uM01 < PivotBoostProb then MaxSpeed * PivotBoostAbs else 0.0

      val turn = (if uT >= 0.0 then 1.0 else -1.0) * clamp(turnCore + pivotBoost, 0.0, MaxSpeed)
      val left = clamp(base - turn, MinSpeed, MaxSpeed)
      val right = clamp(base + turn, MinSpeed, MaxSpeed)
      val action = MovementActionFactory.customMove[F](left, right).getOrElse(NoAction[F]())
      (action, r3)
    }
end RandomWalkBehavior
