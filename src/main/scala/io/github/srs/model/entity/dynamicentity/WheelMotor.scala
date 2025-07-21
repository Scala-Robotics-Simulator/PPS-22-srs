package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Action.applyTo
import io.github.srs.model.validation.Validation

/**
 * WheelMotor is an actuator that controls the movement of a robot.
 */
trait WheelMotor extends Actuator[Robot]:

  /**
   * The time step for the motor's operation, in seconds.
   * @return
   *   the time step as a [[DeltaTime]].
   */
  def dt: DeltaTime

  /**
   * The left wheel of the motor.
   * @return
   *   the left wheel.
   */
  def left: Wheel

  /**
   * The right wheel of the motor.
   * @return
   *   the right wheel.
   */
  def right: Wheel

end WheelMotor

/**
 * Companion object for [[WheelMotor]] providing an extension method to move the robot.
 */
object WheelMotor:

  /**
   * Extension method to move the robot using its wheel motors.
   */
  extension (robot: Robot)

    /**
     * Moves the robot based on the current state of its wheel motors.
     *
     * @return
     *   a new instance of the robot with updated position and orientation.
     */
    def move: Robot =
      robot.actuators.collectFirst { case wm: WheelMotor => wm } match
        case Some(wm) => wm.act(robot).getOrElse(robot)
        case None => robot

    /**
     * Applies a sequence of actions to the robot, updating its state accordingly.
     * @param actions
     *   the sequence of [[Action]] to apply to the robot.
     * @return
     *   the robot with updated state after applying the actions.
     */
    def applyActions(actions: Seq[Action]): Robot =
      actions.foldLeft(robot)((r, a) => a.applyTo(r).move)

  end extension

  /**
   * Creates a new instance of [[WheelMotor]] with the specified time step and wheel configurations.
   * @param dt
   *   the time step for the motor's operation, in seconds.
   * @param left
   *   the left wheel of the motor.
   * @param right
   *   the right wheel of the motor.
   * @return
   *   a new instance of [[WheelMotor]].
   */
  def apply(dt: DeltaTime, left: Wheel, right: Wheel): WheelMotor =
    DifferentialWheelMotor(dt, left, right)

  /**
   * Implementation of the [[WheelMotor]] trait that uses differential drive to move the robot.
   * @param dt
   *   the time step for the motor's operation, in seconds.
   * @param left
   *   the left wheel of the motor.
   * @param right
   *   the right wheel of the motor.
   */
  private case class DifferentialWheelMotor(dt: DeltaTime, left: Wheel, right: Wheel) extends WheelMotor:

    /**
     * Computes the updated position and orientation of a differential-drive robot based on the speeds of its wheels and
     * the time interval `dt`.
     *
     * The robot is assumed to move on a 2D plane, and the orientation is in radians.
     *
     * Physics model:
     *
     *   - The linear velocity of each wheel is obtained by:
     *     {{{
     * v_left = left.speed * left.radius
     * v_right = right.speed * right.radius
     *     }}}
     *   - The linear velocity of the robot is the average of the two:
     *     {{{
     * v = (v_right + v_left) / 2
     *     }}}
     *   - The angular velocity (omega) is proportional to the difference of the wheel velocities:
     *     {{{
     * omega = (v_right - v_left) / d
     *     }}}
     *     Where `d` is the distance between the wheels (assumed to be robot.shape.radius * 2)
     *   - The new position is computed as:
     *     {{{
     * x_new = x + v * cos(theta) * dt
     * y_new = y + v * sin(theta) * dt
     *     }}}
     *   - The new orientation is:
     *     {{{
     * theta_new = theta + omega * dt
     *     }}}
     *
     * @param robot
     *   the robot whose state should be updated.
     * @return
     *   a new [[Robot]] instance with updated position and orientation.
     */
    override def act(robot: Robot): Validation[Robot] =
      val vLeft = this.left.speed * this.left.shape.radius
      val vRight = this.right.speed * this.right.shape.radius
      val wheelDistance = robot.shape.radius * 2
      val velocity = (vLeft + vRight) / 2
      val omega = (vRight - vLeft) / wheelDistance
      val theta = robot.orientation.toRadians
      val dx = velocity * math.cos(theta) * dt.toSeconds
      val dy = velocity * math.sin(theta) * dt.toSeconds
      for
        newPosition <- Point2D(robot.position.x + dx, robot.position.y + dy)
        newOrientation <- Orientation.fromRadians(theta + omega * dt.toSeconds)
        robot <- robot.copy(position = newPosition, orientation = newOrientation)
      yield robot

  end DifferentialWheelMotor
end WheelMotor
