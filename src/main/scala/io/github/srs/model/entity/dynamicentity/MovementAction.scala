package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.dynamicentity.{ Action, Robot, RobotAction }
import io.github.srs.model.validation.Validation

/**
 * MovementAction represents a robot movement action characterized by the speeds applied to the left and right wheels.
 * @param leftSpeed
 *   the speed to apply to the left wheel.
 * @param rightSpeed
 *   the speed to apply to the right wheel.
 * @tparam F
 *   the effect type of the action.
 */
final case class MovementAction[F[_]](leftSpeed: Double, rightSpeed: Double) extends Action[F]:

  /**
   * Runs the movement action using the provided RobotAction.
   *
   * @param ra
   *   the RobotAction to use for executing the movement action.
   * @return
   *   a new instance of Robot after executing the movement action.
   */
  def run(r: Robot)(using ra: RobotAction[F]): F[Robot] =
    ra.moveWheels(r, leftSpeed, rightSpeed)

object MovementActionDsl:

  /**
   * Creates a custom movement action with specified speeds for the left and right wheels.
   *
   * @param left
   *   the speed to apply to the left-wheel motor.
   * @param right
   *   the speed to apply to the right-wheel motor.
   * @return
   *   a [[MovementAction]] with the specified speeds.
   */
  infix def customMove[F[_]](left: Double, right: Double): Validation[MovementAction[F]] =
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ maxSpeed, minSpeed }
    for
      _ <- Validation.bounded("left", left, minSpeed, maxSpeed, includeMax = true)
      _ <- Validation.bounded("right", right, minSpeed, maxSpeed, includeMax = true)
    yield MovementAction(left, right)

  /**
   * Moves the robot forward by applying a positive speed to both wheels.
   */
  infix def moveForward[F[_]]: MovementAction[F] = MovementAction(1.0, 1.0)

  /**
   * Moves the robot backward by applying a negative speed to both wheels.
   */
  infix def moveBackward[F[_]]: MovementAction[F] = MovementAction(-1.0, -1.0)

  /**
   * Turns the robot left by applying a negative speed to the left wheel and a positive speed to the right wheel.
   */
  infix def turnLeft[F[_]]: MovementAction[F] = MovementAction(-0.5, 0.5)

  /**
   * Turns the robot right by applying a positive speed to the left wheel and a negative speed to the right wheel.
   */
  infix def turnRight[F[_]]: MovementAction[F] = MovementAction(0.5, -0.5)

  /**
   * Stops the robot by applying zero speed to both wheels.
   */
  infix def stop[F[_]]: MovementAction[F] = MovementAction(0.0, 0.0)
end MovementActionDsl
