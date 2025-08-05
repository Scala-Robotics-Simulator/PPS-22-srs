package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.validation.Validation

object MovementActionDsl:

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
