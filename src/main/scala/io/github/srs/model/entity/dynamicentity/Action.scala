package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.validation.Validation
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

/**
 * Represents a set of predefined robot movement actions, each characterized by the speeds applied to the left and right
 * wheels.
 */
sealed trait Action:

  /**
   * The speed to apply to the left-wheel motor.
   */
  val leftSpeed: Double

  /**
   * The speed to apply to the right-wheel motor.
   */
  val rightSpeed: Double

  /**
   * Returns the speeds for the left and right wheels as a tuple.
   *
   * @return
   *   a tuple containing the left-wheel and right-wheel speeds.
   */
  def speeds: (Double, Double) = (leftSpeed, rightSpeed)

object Action:

  /**
   * Moves the robot forward by applying a positive speed to both wheels.
   */
  case object MoveForward extends Action:
    val leftSpeed: Double = 1.0
    val rightSpeed: Double = 1.0

  /**
   * Moves the robot backward by applying a negative speed to both wheels.
   */
  case object MoveBackward extends Action:
    val leftSpeed: Double = -1.0
    val rightSpeed: Double = -1.0

  /**
   * Turns the robot left by applying a negative speed to the left wheel and a positive speed to the right wheel.
   */
  case object TurnLeft extends Action:
    val leftSpeed: Double = -0.5
    val rightSpeed: Double = 0.5

  /**
   * Turns the robot right by applying a positive speed to the left wheel and a negative speed to the right wheel.
   */
  case object TurnRight extends Action:
    val leftSpeed: Double = 0.5
    val rightSpeed: Double = -0.5

  /**
   * Stops the robot by applying zero speed to both wheels.
   */
  case object Stop extends Action:
    val leftSpeed: Double = 0.0
    val rightSpeed: Double = 0.0

  /**
   * Represents a custom movement action with specified speeds for the left and right wheels.
   * @param leftSpeed
   *   the speed to apply to the left-wheel motor.
   * @param rightSpeed
   *   the speed to apply to the right-wheel motor.
   */
  private final case class CustomMovement(override val leftSpeed: Double, override val rightSpeed: Double)
      extends Action

  /**
   * Creates a custom movement action with specified speeds for the left and right wheels.
   * @param left
   *   the speed to apply to the left-wheel motor.
   * @param right
   *   the speed to apply to the right-wheel motor.
   * @return
   *   a [[Validation]] containing the custom movement action if the speeds are valid, or an error message if they are
   *   not.
   */
  def move(left: Double, right: Double): Validation[Action] =
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ maxSpeed, minSpeed }
    for
      _ <- Validation.bounded("left wheel speed", left, minSpeed, maxSpeed, includeMax = true)
      _ <- Validation.bounded("right wheel speed", right, minSpeed, maxSpeed, includeMax = true)
    yield CustomMovement(left, right)

    /**
     * Extension method for the [[Action]] enum to apply the action to a robot.
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
      robot.actuators.collectFirst { case wm: WheelMotor => wm } match
        case Some(wm) =>
          val (leftSpeed, rightSpeed) = action.speeds
          val updatedActuator = WheelMotor(
            wm.left.updatedSpeed(leftSpeed),
            wm.right.updatedSpeed(rightSpeed),
          )
          val updatedActuators = robot.actuators.map:
            case _: WheelMotor => updatedActuator
            case other => other
          (robot withActuators updatedActuators).validate.getOrElse(robot)
        case None => robot
  end extension
end Action
