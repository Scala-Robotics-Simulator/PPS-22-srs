package io.github.srs.model.entity.dynamicentity

/**
 * RobotAction trait defines the actions that can be performed on a robot.
 *
 * It represents the algebra of actions that can be executed on a robot, such as, for example, moving the wheels or
 * stopping them.
 * @tparam F
 *   the effect type of the action.
 */
trait RobotAction[F[_]]:
  /**
   * Moves the robot's wheels with the specified speeds.
   * @param robot
   *   the robot on which the action will be executed.
   * @param leftSpeed
   *   the speed to apply to the left wheel.
   * @param rightSpeed
   *   the speed to apply to the right wheel.
   * @return
   *   a new instance of Robot with updated wheel speeds.
   */
  def moveWheels(robot: Robot, leftSpeed: Double, rightSpeed: Double): F[Robot]
