package io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors

import cats.{ Id, Monad }
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorTypes.{ Behavior, Condition }
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.Behaviors
import io.github.srs.model.entity.dynamicentity.robot.behavior.dsl.BehaviorDsl.*
import io.github.srs.utils.random.RNG

import BehaviorCommon.*

/**
 * A behavior that prioritizes obstacle avoidance, then phototaxis, then random walk.
 *
 * If an obstacle is too close, it will avoid it. If there is light detected, it will move towards it. If neither of
 * those conditions is met, it will perform a random walk.
 */
object PrioritizedBehavior:

  /**
   * The decision function for the prioritized behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[BehaviorCommon.Decision]] that computes the action based on prioritized conditions.
   */
  def decision[F[_]: Monad]: Decision[F] =

    val danger: Condition[BehaviorContext] =
      ctx => ctx.sensorReadings.proximityReadings.exists(_.value < Behaviors.Prioritized.DangerDist)

    val hasLight: Condition[BehaviorContext] =
      ctx => ctx.sensorReadings.lightReadings.exists(_.value >= Behaviors.Prioritized.LightThreshold)

    val chooser: Behavior[BehaviorContext, BehaviorContext => (Action[F], RNG)] =
      (
        (danger ==> ObstacleAvoidanceBehavior.decision[F].run) |
          (hasLight ==> PhototaxisBehavior.decision[F].run)
      ).default(RandomWalkBehavior.decision[F].run)

    Kleisli.ask[Id, BehaviorContext].flatMap { ctx =>
      Kleisli.liftF(chooser.run(ctx)(ctx))
    }
end PrioritizedBehavior
