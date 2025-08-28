package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Monad
import cats.data.Kleisli
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.action.{ MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.Behaviors.ObstacleAvoidance.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }

/**
 * A behavior that avoids obstacles based on proximity sensor readings.
 *
 * It attempts to move away from the closest obstacle while maintaining forward movement when possible. If an obstacle
 * is too close, it will pivot away from it.
 */
object ObstacleAvoidanceBehavior:

  def decision[F[_]: Monad]: Decision[F] =
    Kleisli { ctx =>
      val readings = ctx.sensorReadings.proximityReadings

      val closestOpt: Option[(Double, Orientation)] =
        readings.foldLeft(Option.empty[(Double, Orientation)]) { (acc, r) =>
          val cur = (r.value, r.sensor.offset)
          acc match
            case None => Some(cur)
            case Some((best, _)) => if r.value < best then Some(cur) else acc
        }

      val (rx, ry) =
        readings.foldLeft((0.0, 0.0)) { case ((sx, sy), r) =>
          val w = clamp01(r.value)
          val rad = math.toRadians(r.sensor.offset.degrees)
          (sx + w * math.cos(rad), sy + w * math.sin(rad))
        }

      val vecMag = math.hypot(rx, ry)
      val vecDegOpt =
        if vecMag > ResultantEps then Some(math.toDegrees(math.atan2(ry, rx))) else None

      val (lSum, lCnt, rSum, rCnt) =
        readings.foldLeft((0.0, 0, 0.0, 0)) { case ((ls, lc, rs, rc), r) =>
          val deg = toSignedDegrees(r.sensor.offset.degrees)
          val v = clamp01(r.value)
          if deg > 0.0 then (ls + v, lc + 1, rs, rc) else (ls, lc, rs + v, rc + 1)
        }
      val leftAvg = if lCnt == 0 then 0.0 else lSum / lCnt.toDouble
      val rightAvg = if rCnt == 0 then 0.0 else rSum / rCnt.toDouble
      val sideDelta = leftAvg - rightAvg

      val fallbackDeg =
        if math.abs(sideDelta) > SideDeltaEps then if sideDelta > 0.0 then FallbackLeftDeg else -FallbackLeftDeg
        else FallbackLeftDeg

      val freeDegOpt = vecDegOpt.orElse(Some(fallbackDeg))

      val action =
        (closestOpt, freeDegOpt) match
          case (None, _) => MovementActionFactory.moveForward[F]

          case (Some((distNorm, _)), Some(freeDeg)) =>
            val signed = toSignedDegrees(freeDeg)
            val absAng = math.abs(signed)

            if distNorm >= SafeDist then MovementActionFactory.moveForward[F]
            else if distNorm < CriticalDist then
              val s = if signed >= 0.0 then 1.0 else -1.0
              val v = MaxSpeed * PivotSpeedFactor
              MovementActionFactory.customMove[F](-s * v, s * v).getOrElse(NoAction[F]())
            else
              val forward =
                clamp(
                  MaxSpeed * math
                    .max(MinForward, distNorm / SafeDist),
                  MinSpeed,
                  MaxSpeed,
                )
              val turnMag = clamp(MaxSpeed * (absAng / 180.0), 0.0, MaxSpeed)
              val (lRaw, rRaw) =
                if signed >= 0.0 then (forward - turnMag, forward + turnMag) else (forward + turnMag, forward - turnMag)
              val left = clamp(lRaw, MinSpeed, MaxSpeed)
              val right = clamp(rRaw, MinSpeed, MaxSpeed)
              MovementActionFactory.customMove[F](left, right).getOrElse(NoAction[F]())

          case _ => MovementActionFactory.moveForward[F]

      (action, ctx.rng)
    }
end ObstacleAvoidanceBehavior
