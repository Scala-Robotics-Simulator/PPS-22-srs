package io.github.srs.model.entity.dynamicentity.actuator

import scala.concurrent.duration.FiniteDuration

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor

/**
 * DifferentialKinematics provides methods to compute the kinematics of a differential drive robot.
 *
 * It includes methods to compute wheel velocities, robot velocities, and the new position and orientation of the robot
 * based on its current state and wheel speeds.
 */
object DifferentialKinematics:

  /**
   * Computes the linear velocities of the left and right wheels based on their speeds and radii.
   *
   * The linear velocity of each wheel is obtained by:
   * {{{
   * vLeft = left.speed * left.radius
   * vRight = right.speed * right.radius
   * }}}
   *
   * @return
   *   a function that takes a [[DifferentialWheelMotor]] and returns a tuple containing the linear velocities of the
   *   left and right wheels.
   */
  def computeWheelVelocities: DifferentialWheelMotor => (Double, Double) =
    motor =>
      val vLeft = motor.left.speed * motor.left.shape.radius
      val vRight = motor.right.speed * motor.right.shape.radius
      (vLeft, vRight)

  /**
   * Computes the linear velocity and angular velocity of the robot based on the wheel velocities.
   *
   *   - The linear velocity of the robot is the average of the two:
   *     {{{
   * v = (vRight + vLeft) / 2
   *     }}}
   *   - The angular velocity (omega) is proportional to the difference of the wheel velocities:
   *     {{{
   * omega = (vRight - vLeft) / d
   *     }}}
   *     Where `d` is the distance between the wheels (assumed to be robot.shape.radius * 2)
   *
   * @param wheelDistance
   *   the distance between the left and right wheels.
   * @return
   *   a function (vLeft, vRight) => (v, omega) that computes the linear and angular velocities of the robot.
   */
  def computeVelocities(wheelDistance: Double): ((Double, Double)) => (Double, Double) =
    (vLeft, vRight) =>
      val v = (vLeft + vRight) / 2
      val omega = (vRight - vLeft) / wheelDistance
      (v, omega)

  /**
   * Computes the new position and orientation of the robot based on its current orientation `theta` and the time.
   *
   *   - The new position is computed as:
   *     {{{
   * xNew = x + v * cos(theta) * dt
   * yNew = y + v * sin(theta) * dt
   *     }}}
   *   - The new orientation is:
   *     {{{
   * thetaNew = theta + omega * dt
   *     }}}
   *
   * @param theta
   *   the current orientation of the robot in radians.
   * @param dt
   *   the time interval over which the robot moves. This is a [[FiniteDuration]] representing the time step for the
   *   movement.
   * @return
   *   a function (v, omega) => (dx, dy, newOrientation) that computes the change in position and the new orientation of
   *   the robot.
   */
  def computePositionAndOrientation(
      theta: Double,
      dt: FiniteDuration,
  ): ((Double, Double)) => (Double, Double, Orientation) =
    (v, omega) =>
      val dtSeconds = dt.toSeconds
      val dx = v * math.cos(theta) * dtSeconds
      val dy = v * math.sin(theta) * dtSeconds
      val newTheta = theta + omega * dtSeconds
      (dx, dy, Orientation.fromRadians(newTheta))
end DifferentialKinematics
