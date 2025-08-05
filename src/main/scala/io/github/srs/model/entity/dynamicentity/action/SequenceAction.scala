package io.github.srs.model.entity.dynamicentity.action

import cats.Monad
import cats.syntax.foldable.toFoldableOps
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.RobotAction

/**
 * SequenceAction represents a composite action that executes a sequence of actions in order.
 *
 * @param actions
 *   the list of actions to be executed in sequence.
 * @param monad$F$0
 *   the implicit Monad instance for the effect type F.
 * @tparam F
 *   the effect type of the action.
 */
private final case class SequenceAction[F[_]: Monad](actions: List[Action[F]]) extends Action[F]:

  /**
   * Runs the sequence of actions on the given robot.
   * @param robot
   *   the robot on which the actions will be executed.
   * @param ra
   *   the RobotAction to use for executing the action.
   * @return
   *   a new instance of Robot after executing the action.
   */
  override def run(robot: Robot)(using ra: RobotAction[F]): F[Robot] =
    actions.foldLeftM(robot)((r, act) => act.run(r))

/**
 * NoAction represents an action that does nothing.
 *
 * @param monad$F$0
 *   the implicit Monad instance for the effect type F.
 * @tparam F
 *   the effect type of the action.
 */
final case class NoAction[F[_]: Monad]() extends Action[F]:

  /**
   * Runs the no-action on the given robot.
   * @param robot
   *   the robot on which the action will be executed.
   * @param ra
   *   the RobotAction to use for executing the action.
   * @return
   *   a new instance of Robot after executing the action (which is a no-op).
   */
  override def run(robot: Robot)(using ra: RobotAction[F]): F[Robot] =
    Monad[F].pure(robot)
