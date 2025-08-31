package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Monad
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.Behaviors

/**
 * A behavior that prioritizes obstacle avoidance, then phototaxis, then random walk.
 *
 * If an obstacle is too close, it will avoid it. If there is light detected, it will move towards it. If neither of
 * those conditions is met, it will perform a random walk.
 */
object PrioritizedBehavior:

  def decision[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      val hazard =
        ctx.sensorReadings.proximityReadings.exists(_.value < Behaviors.Prioritized.DangerDist)

      if hazard then ObstacleAvoidanceBehavior.decision[F].run(ctx)
      else
        val hasLight =
          ctx.sensorReadings.lightReadings.exists(_.value >= Behaviors.Prioritized.LightThreshold)
        if hasLight then PhototaxisBehavior.decision[F].run(ctx)
        else RandomWalkBehavior.decision[F].run(ctx)
    }
