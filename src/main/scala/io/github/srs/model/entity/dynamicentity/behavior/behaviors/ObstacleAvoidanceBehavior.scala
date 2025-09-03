package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.data.Kleisli
import cats.{ Id, Monad }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.BehaviorCommon.*
import io.github.srs.model.entity.dynamicentity.sensor.ProximityReadings
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.utils.SimulationDefaults.Behaviors.ObstacleAvoidance.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }

/**
 * A [[Behavior]] that avoid obstacles using proximity sensor readings.
 *
 * The behavior operates in three phases:
 *
 *   - [[Free]]: No obstacles detected, the robot moves forward at cruise speed.
 *   - [[Warn]]: Obstacles detected but not critical, the robot slows down and steers away from the closest obstacle.
 *   - [[Blocked]]: Obstacles detected at a critical distance, the robot performs a pivot turn
 */
object ObstacleAvoidanceBehavior:

  /**
   * The decision function for the obstacle avoidance behavior.
   *
   * @tparam F
   *   The effect type.
   * @return
   *   A [[Decision]] that computes the action based on proximity sensor readings.
   */
  def decision[F[_]: Monad]: Decision[F] =
    Kleisli.ask[Id, BehaviorContext].map { ctx =>
      val readings = ctx.sensorReadings.proximityReadings

      val front = minIn(readings)(deg => math.abs(deg) <= 10)
      val frontLeft = minIn(readings)(deg => deg > 0.0 && deg <= 100.0)
      val frontRight = minIn(readings)(deg => deg < 0.0 && deg >= -100.0)

      val phase = pickPhase(frontLeft, frontRight)
      val sign = turnSign(readings)

      val (l, r) =
        if front < CriticalDist then (-1.0, -0.1)
        else
          phase match
            case Phase.Free => (CruiseSpeed, CruiseSpeed)
            case Phase.Warn =>
              if sign > 0.0 then (WarnSpeed - WarnTurnSpeed, WarnSpeed + WarnTurnSpeed)
              else (WarnSpeed + WarnTurnSpeed, WarnSpeed - WarnTurnSpeed)
            case Phase.Blocked =>
              if sign > 0.0 then (-BackBoost, BackBoost)
              else (BackBoost, -BackBoost)

      (wheels[F](l, r), ctx.rng)
    }

  /**
   * The operational phase of the obstacle avoidance behavior.
   */
  private enum Phase derives CanEqual:
    case Free, Warn, Blocked

  /**
   * Filters proximity sensor readings by angle and returns the minimum reading value. The value is clamped between 0
   * (obstacle very close) and 1 (no obstacle).
   *
   * @param readings
   *   The proximity sensor readings.
   * @param keep
   *   A predicate function to filter sensor angles.
   * @return
   *   The minimum clamped reading value, or 1.0 if no readings match the filter.
   */
  private def minIn(readings: ProximityReadings)(keep: Double => Boolean): Double =
    (for
      r <- readings.iterator
      signedDeg = toSignedDegrees(r.sensor.offset.degrees)
      if keep(signedDeg)
    yield clamp01(r.value)).minOption.getOrElse(1.0)

  /**
   * Calculates the average "freedom" (0 = blocked, 1 = free) for the left and right hemispheres based on proximity
   * sensor readings. ¶
   * @param readings
   *   The proximity sensor readings.
   * @return
   *   A tuple containing the average freedom for the left and right hemispheres.
   */
  private def hemisphereAverages(readings: ProximityReadings): (Double, Double) =
    val (sumL, cntL, sumR, cntR) =
      readings.foldLeft((0.0, 0, 0.0, 0)) { case ((sl, cl, sr, cr), r) =>
        val d = toSignedDegrees(r.sensor.offset.degrees)
        val v = clamp01(r.value)
        if d > 0.0 then (sl + v, cl + 1, sr, cr) else (sl, cl, sr + v, cr + 1)
      }
    val avgL = if cntL == 0 then 1.0 else sumL / cntL
    val avgR = if cntR == 0 then 1.0 else sumR / cntR
    (avgL, avgR)

  /**
   * Determines the operational phase of the obstacle avoidance behavior based on proximity sensor readings.
   *
   * @param frontLeft
   *   The proximity reading from the front-left sensor.
   * @param frontRight
   *   The proximity reading from the front-right sensor.
   * @return
   *   The determined [[Phase]].
   */
  private def pickPhase(frontLeft: Double, frontRight: Double): Phase =
    val minFront = math.min(frontLeft, frontRight)
    if minFront <= CriticalDist then Phase.Blocked
    else if minFront < SafeDist then Phase.Warn
    else Phase.Free

  /**
   * Determines the turning speed of the robot based on proximity sensor readings.
   *
   * The method compares the average "freedom" values for the left and right hemispheres to decide which direction the
   * robot should turn. A higher average "freedom" on the left side results in a higher turning speed to the left, while
   * a higher value on the right leads to turning right.
   *
   * @param readings
   *   The proximity sensor readings, used to calculate the average "freedom" for the left and right hemispheres.
   * @return
   *   The turning speed:
   *   - Returns the maximum speed if the left side has more "freedom."
   *   - Returns the minimum speed if the right side has more "freedom."
   *   - Returns the maximum speed if both sides have equal "freedom."
   */
  private def turnSign(readings: ProximityReadings): Double =
    val (avgLeft, avgRight) = hemisphereAverages(readings)
    if avgLeft > avgRight then MaxSpeed // più “libero” a sinistra → gira a sinistra
    else if avgLeft < avgRight then MinSpeed
    else MaxSpeed

end ObstacleAvoidanceBehavior
