package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.Robot

/**
 * Action trait represents an action that can be performed on a robot.
 * @tparam F
 *   the effect type of the action.
 */
trait Action[F[_]]:
  /**
   * Runs the action using the provided RobotAction.
   * @param r
   *   the Robot on which the action will be executed.
   * @param ra
   *   the RobotAction to use for executing the action.
   * @return
   *   a new instance of Robot after executing the action.
   */
  def run(r: Robot)(using ra: RobotAction[F]): F[Robot]
