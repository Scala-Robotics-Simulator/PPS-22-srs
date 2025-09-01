package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Monad
import cats.data.Kleisli
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.action.{ MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.Behaviors.Phototaxis.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }

/**
 * A behavior that moves towards the strongest light source based on light sensor readings.
 *
 * It calculates the best direction to move towards the light and adjusts its wheel speeds accordingly. If no light is
 * detected, it defaults to moving forward.
 */
object PhototaxisBehavior:

  def decision[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      val best: Option[(Double, Orientation)] =
        ctx.sensorReadings.lightReadings.foldLeft(Option.empty[(Double, Orientation)]) { (acc, r) =>
          val cur = (r.value, r.sensor.offset)
          acc match
            case None => Some(cur)
            case Some((bestVal, bestOff)) =>
              val (v, off) = cur
              if v > bestVal + Epsilon then Some(cur)
              else if math.abs(v - bestVal) <= Epsilon && absSigned(
                  off.degrees,
                ) < absSigned(
                  bestOff.degrees,
                )
              then Some(cur)
              else acc
        }

      val action =
        best match
          case None => MovementActionFactory.moveForward[F]
          case Some((strengthRaw, off)) =>
            val strength = clamp01(strengthRaw)
            val ang = toSignedDegrees(off.degrees)
            val err01 = math.abs(ang) / 180.0

            val base =
              clamp(
                MaxSpeed * (MinForwardBias +
                  (1.0 - MinForwardBias) * strength),
                MinSpeed,
                MaxSpeed,
              )

            val turnMag = clamp(
              MaxSpeed * (TurnGain * err01),
              0.0,
              MaxSpeed,
            )
            val (lRaw, rRaw) = if ang > 0.0 then (base - turnMag, base + turnMag) else (base + turnMag, base - turnMag)
            val left = clamp(lRaw, MinSpeed, MaxSpeed)
            val right = clamp(rRaw, MinSpeed, MaxSpeed)
            MovementActionFactory.customMove[F](left, right).getOrElse(NoAction[F]())

      (action, ctx.rng)
    }
end PhototaxisBehavior
