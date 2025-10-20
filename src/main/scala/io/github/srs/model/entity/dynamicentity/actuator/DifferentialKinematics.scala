package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.FiniteDuration
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity

given DurationToDouble: Conversion[FiniteDuration, Double] with
  def apply(d: FiniteDuration): Double = d.toMillis / 1000.0

/**
 * DifferentialKinematics provides methods to compute the kinematics of a differential drive dynamic entity.
 */
object DifferentialKinematics:

  /**
   * Computes the linear velocities of the left and right wheels from motor speeds and wheel radii.
   */
  def computeWheelVelocities[E <: DynamicEntity]: DifferentialWheelMotor[E] => (Double, Double) =
    (motor: DifferentialWheelMotor[E]) =>
      val vLeft  = motor.left.speed  * motor.left.shape.radius
      val vRight = motor.right.speed * motor.right.shape.radius
      (vLeft, vRight)

  /**
   * Computes (v, omega) from (vLeft, vRight).
   */
  def computeVelocities(wheelDistance: Double): ((Double, Double)) => (Double, Double) =
    (vels: (Double, Double)) =>
      val (vLeft, vRight) = vels
      val v     = (vLeft + vRight) / 2
      val omega = (vRight - vLeft) / wheelDistance
      (v, omega)

  /**
   * From (v, omega), current theta and dt, returns (dx, dy, newOrientation).
   */
  def computePositionAndOrientation(
                                     theta: Double,
                                     dt: FiniteDuration,
                                   ): ((Double, Double)) => (Double, Double, Orientation) =
    (vo: (Double, Double)) =>
      val (v, omega) = vo
      val dtSeconds: Double = dt
      val dx       = v * math.cos(theta) * dtSeconds
      val dy       = v * math.sin(theta) * dtSeconds
      val newTheta = theta + omega * dtSeconds
      (dx, dy, Orientation.fromRadians(newTheta))
end DifferentialKinematics
