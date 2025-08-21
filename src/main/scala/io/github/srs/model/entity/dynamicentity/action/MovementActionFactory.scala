package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.validation.Validation
import io.github.srs.utils.SimulationDefaults.DynamicEntity.*

/**
 * Factory object for creating movement actions for dynamic entities. Provides methods to create custom movement actions
 * with specified speeds for the left and right wheels, as well as predefined actions for moving forward, backward,
 * turning left, turning right, and stopping.
 */
object MovementActionFactory:

  /**
   * Creates a custom movement action with specified speeds for the left and right wheels.
   * @param left
   *   the speed to apply to the left wheel.
   * @param right
   *   the speed to apply to the right wheel.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   a [[Validation]] containing the [[MovementAction]] if the speeds are within the defined bounds, or an error if
   *   they are not.
   */
  infix def customMove[F[_]](left: Double, right: Double): Validation[MovementAction[F]] =
    for
      _ <- Validation.bounded("left", left, minSpeed, maxSpeed, includeMax = true)
      _ <- Validation.bounded("right", right, minSpeed, maxSpeed, includeMax = true)
    yield MovementAction(left, right)

  /**
   * Moves the robot forward by applying a positive speed to both wheels.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   the [[MovementAction]] representing the forward movement.
   */
  infix def moveForward[F[_]]: MovementAction[F] = MovementAction(maxSpeed, maxSpeed)

  /**
   * Moves the robot backward by applying a negative speed to both wheels.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   the [[MovementAction]] representing the backward movement.
   */
  infix def moveBackward[F[_]]: MovementAction[F] = MovementAction(-maxSpeed, -maxSpeed)

  /**
   * Turns the robot left by applying a positive speed to the left wheel and a negative speed to the right wheel.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   the [[MovementAction]] representing the left turn.
   */
  infix def turnLeft[F[_]]: MovementAction[F] = MovementAction(halfSpeed, -halfSpeed)

  /**
   * Turns the robot right by applying a negative speed to the left wheel and a positive speed to the right wheel.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   the [[MovementAction]] representing the right turn.
   */
  infix def turnRight[F[_]]: MovementAction[F] = MovementAction(-halfSpeed, halfSpeed)

  /**
   * Stops the robot by applying zero speed to both wheels.
   * @tparam F
   *   the effect type of the action.
   * @return
   *   the [[MovementAction]] representing the stop action.
   */
  infix def stop[F[_]]: MovementAction[F] = MovementAction(zeroSpeed, zeroSpeed)
end MovementActionFactory

export io.github.srs.model.entity.dynamicentity.action.SequenceAction.thenDo
