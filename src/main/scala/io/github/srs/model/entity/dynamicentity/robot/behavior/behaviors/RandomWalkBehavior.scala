package io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors

import cats.data.Kleisli
import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.utils.SimulationDefaults.Behaviors.RandomWalk.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }

import BehaviorCommon.*

/**
 * A [[io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior]] that makes the entity perform a random
 * walk.
 *
 * It randomly selects speeds for the left and right wheels, with a bias towards forward movement. Occasionally, it may
 * perform a pivot turn to change a direction more sharply.
 */
object RandomWalkBehavior:

  /**
   * The decision function for the random walk behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[BehaviorCommon.Decision]] that computes the action based on random values.
   */
  def decision[F[_]: Monad]: Decision[F] =
    Kleisli.ask[Id, BehaviorContext].map { ctx =>
      import io.github.srs.utils.random.RandomDSL.*
      val range = MinSpeed to MaxSpeed

      val (uF, r1) = ctx.rng generate range.includeMax
      val (uT, r2) = r1 generate range.includeMax
      val (uM, r3) = r2 generate range.includeMax

      val base =
        clamp(MaxSpeed * (MinForwardFactor + MaxForwardExtra * math.abs(uF)), MinSpeed, MaxSpeed)

      val minTurn = base * MinTurnOfBase
      val maxTurn = math.min(MaxSpeed, base * MaxTurnOfBase)
      val turnCore = minTurn + (maxTurn - minTurn) * math.pow(math.abs(uT), TurnExponent)

      val span = MaxSpeed - MinSpeed
      val uM01 = if span == 0.0 then 0.5 else (uM - MinSpeed) / span
      val pivotBoost = if uM01 < PivotBoostProb then MaxSpeed * PivotBoostAbs else 0.0

      val turn = math.signum(uT) * clamp(turnCore + pivotBoost, 0.0, MaxSpeed)
      val left = clamp(base - turn, MinSpeed, MaxSpeed)
      val right = clamp(base + turn, MinSpeed, MaxSpeed)

      (moveOrNo[F](left, right), r3)
    }
end RandomWalkBehavior
