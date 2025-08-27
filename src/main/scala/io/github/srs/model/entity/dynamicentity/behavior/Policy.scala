package io.github.srs.model.entity.dynamicentity.behavior

import cats.Monad
import cats.data.Kleisli
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.behavior.Policy.*
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity
import io.github.srs.utils.random.RNG

/**
 * A [[Policy]] selects the next [[Action]] purely, based only on the provided [[BehaviorContext]] and returns the next
 * RNG alongside the action.
 */
enum Policy(val name: String) derives CanEqual:
  case AlwaysForward extends Policy("AlwaysForward")
  case RandomWalk extends Policy("RandomWalk")
  case ObstacleAvoidance extends Policy("ObstacleAvoidance")

  /**
   * Compute the next [[Action]] and the advanced RNG.
   *
   * @param input
   *   behavior input containing the current RNG (and other context)
   * @return
   *   (selected action, next RNG)
   */
  def run[F[_]: Monad](input: BehaviorContext): (Action[F], RNG) =
    this match
      case AlwaysForward => alwaysForwardBehavior.run(input)
      case RandomWalk => randomWalkBehavior.run(input)
      case ObstacleAvoidance => obstacleAvoidanceBehavior.run(input)

  /**
   * String representation of the policy.
   * @return
   *   name of the policy
   */
  override def toString: String = name

end Policy

/**
 * Ready-made behaviors.
 *
 * These can be used directly or as building blocks for more complex behaviors.
 */
object Policy:

  /**
   * Type alias for a decision behavior that produces an action and the next RNG.
   * @param F
   *   the effect type
   * @return
   *   a behavior that produces an [[Action]] and the next [[RNG]]
   */
  private type Decision[F[_]] = Behavior[BehaviorContext, (Action[F], RNG)]

  def fromString(s: String): Option[Policy] =
    values.find(_.name == s)

  /**
   * always move forward.
   * @tparam F
   *   the effect type
   * @return
   *   a decision that always produces a move forward [[Action]] and same [[RNG]]
   */
  private def alwaysForwardBehavior[F[_]]: Decision[F] =
    Kleisli { ctx =>
      (MovementActionFactory.moveForward[F], ctx.rng)
    }

  /**
   * Random walk: random left/right wheel speeds in [-1.0, 1.0].
   *
   * @tparam F
   *   the effect type
   * @return
   *   a decision that produces a random movement [[Action]] and the next [[RNG]]
   */
  private def randomWalkBehavior[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      import io.github.srs.utils.random.RandomDSL.*
      val range = DynamicEntity.minSpeed to DynamicEntity.maxSpeed
      val (w1, r1) = ctx.rng generate range.includeMax
      val (w2, r2) = r1 generate range.includeMax
      val action =
        MovementActionFactory.customMove[F](w1, w2).getOrElse(NoAction[F]())
      (action, r2)
    }

  private def obstacleAvoidanceBehavior[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      val proximityReadings = ctx.sensorReadings.proximityReadings.map(r => r.value -> r.sensor.offset)

      if proximityReadings.isEmpty then (MovementActionFactory.moveForward[F], ctx.rng)
      else
        val (closestDist, closestOffset) = proximityReadings.foldLeft((Double.MaxValue, Orientation(0.0))):
          case ((minDist, minOffset), (dist, offset)) =>
            if dist < minDist then (dist, offset) else (minDist, minOffset)

        val threshold = 0.6
        val safeMin = 0.3
        val action =
          if closestDist < threshold then
            println(s"Closest obstacle at distance $closestDist with offset ${closestOffset.degrees}°")
            if closestOffset.degrees != 90.0 &&
              closestOffset.degrees != 135.0 &&
              closestOffset.degrees != 180.0 &&
              closestOffset.degrees != 225.0 &&
              closestOffset.degrees != 270.0 &&
              closestDist < safeMin
            then MovementActionFactory.moveBackward[F]
            else
              val turn = if closestOffset.degrees >= 0 && closestOffset.degrees <= 180 then -0.9 else 0.9
              MovementActionFactory.customMove[F](0.5, turn).getOrElse(NoAction[F]())
          else MovementActionFactory.moveForward[F]

        (action, ctx.rng)
      end if
    }

end Policy
