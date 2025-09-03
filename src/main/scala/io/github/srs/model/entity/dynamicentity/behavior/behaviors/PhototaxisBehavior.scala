package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.data.Kleisli
import cats.{ Id, Monad }
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.model.entity.dynamicentity.sensor.LightReadings
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

  /**
   * The decision function for the phototaxis behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[Decision]] that computes the action based on light sensor readings.
   */
  def decision[F[_]: Monad]: Decision[F] =
    Kleisli.ask[Id, BehaviorContext].map { ctx =>
      val action =
        bestLight(ctx.sensorReadings.lightReadings) match
          case None => MovementActionFactory.moveForward[F]
          case Some((s, off)) =>
            val (l, r) = wheelsTowards(s, off)
            moveOrNo[F](l, r)

      (action, ctx.rng)
    }

  /**
   * Determines the best light reading from a collection of light sensor readings.
   *
   * The "best" light is determined based on the highest light intensity value. If there are multiple readings with
   * equal intensity within a small tolerance (`Epsilon`), the reading with the smallest absolute orientation offset is
   * selected.
   *
   * @param readings
   *   A collection of light sensor readings
   * @return
   *   An optional tuple containing the best light intensity value and the corresponding orientation.
   */
  private def bestLight(readings: LightReadings): Option[(Double, Orientation)] =
    readings.foldLeft(Option.empty[(Double, Orientation)]) { (acc, r) =>
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

  /**
   * Calculates the velocities of the left and right wheels to move towards a target orientation.
   *
   * @param strengthRaw
   *   The raw attraction strength towards the target.
   * @param off
   *   The orientation offset from the target.
   * @return
   *   A tuple containing the calculated velocities for the left and right wheels.
   */
  private def wheelsTowards(strengthRaw: Double, off: Orientation): (Double, Double) =
    val strength = clamp01(strengthRaw)
    val ang = toSignedDegrees(off.degrees)
    val err01 = math.abs(ang) / 180.0

    val base =
      clamp(
        MaxSpeed * (MinForwardBias + (1.0 - MinForwardBias) * strength),
        MinSpeed,
        MaxSpeed,
      )

    val turnMag = clamp(MaxSpeed * (TurnGain * err01), 0.0, MaxSpeed)
    val (lRaw, rRaw) =
      if ang > 0.0 then (base - turnMag, base + turnMag)
      else (base + turnMag, base - turnMag)

    (clamp(lRaw, MinSpeed, MaxSpeed), clamp(rRaw, MinSpeed, MaxSpeed))
end PhototaxisBehavior
