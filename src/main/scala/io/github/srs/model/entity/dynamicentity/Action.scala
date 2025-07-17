package io.github.srs.model.entity.dynamicentity

/**
 * Represents a set of predefined robot movement actions, each characterized by the speeds applied to the left and right
 * wheels.
 *
 * @param speedLeft
 *   the speed to apply to the left wheel motor.
 * @param speedRight
 *   the speed to apply to the right wheel motor.
 */
enum Action(val speedLeft: Double, val speedRight: Double):
  /**
   * Moves the robot forward by applying a positive speed to both wheels.
   */
  case MoveForward extends Action(1.0, 1.0)

  /**
   * Moves the robot backward by applying a negative speed to both wheels.
   */
  case MoveBackward extends Action(-1.0, -1.0)

  /**
   * Turns the robot left by applying a negative speed to the left wheel and a positive speed to the right wheel.
   */
  case TurnLeft extends Action(-0.5, 0.5)

  /**
   * Turns the robot right by applying a positive speed to the left wheel and a negative speed to the right wheel.
   */
  case TurnRight extends Action(0.5, -0.5)

  /**
   * Stops the robot by applying zero speed to both wheels.
   */
  case Stop extends Action(0.0, 0.0)

  /**
   * Returns the speeds for the left and right wheels as a tuple.
   *
   * @return
   *   a tuple containing the left and right wheel speeds.
   */
  def speeds: (Double, Double) = (speedLeft, speedRight)

end Action

/**
 * Companion object for the [[Action]] enum, providing an extension method.
 */
object Action:

  /**
   * Extension method to apply the action to a robot, updating its wheel motors accordingly.
   *
   * @param action
   *   the action to apply.
   * @return
   *   a new [[Robot]] instance with updated wheel motor speeds.
   */
  extension (action: Action)

    /**
     * Applies the action to the given robot, updating its wheel motors with the specified speeds.
     *
     * @param robot
     *   the robot to which the action will be applied.
     * @return
     *   a new instance of [[Robot]] with updated wheel motor speeds.
     */
    def applyTo(robot: Robot): Robot =
      robot.actuators.collectFirst { case wm: WheelMotor =>
        val (leftSpeed, rightSpeed) = action.speeds

        val updatedActuator = WheelMotor(
          wm.dt,
          wm.left.updatedSpeed(leftSpeed),
          wm.right.updatedSpeed(rightSpeed),
        )

        val updatedActuators: Seq[Actuator[Robot]] =
          robot.actuators.map:
            case _: WheelMotor => updatedActuator
            case other => other

        Robot(robot.position, robot.shape, robot.orientation, updatedActuators)
      }.getOrElse(robot)
  end extension
end Action
