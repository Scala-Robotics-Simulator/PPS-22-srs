package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.Robot

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
