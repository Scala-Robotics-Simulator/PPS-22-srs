package io.github.srs.model.entity.dynamicentity

import scala.concurrent.duration.FiniteDuration

import cats.Monad
import cats.syntax.flatMap.toFlatMapOps
import io.github.srs.model.entity.*
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.{ Action, RobotAction }
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

/**
 * WheelMotor is an actuator that controls the movement of a robot.
 */
trait DifferentialWheelMotor extends Actuator[Robot]:

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

/**
 * Companion object for [[DifferentialWheelMotor]] providing an extension method to move the robot.
 */
object DifferentialWheelMotor:

  /**
   * Creates a new instance of [[DifferentialWheelMotor]] with the specified time step and wheel configurations.
   *
   * @param left
   *   the left wheel of the motor.
   * @param right
   *   the right wheel of the motor.
   * @return
   *   a new instance of [[DifferentialWheelMotor]].
   */
  def apply(left: Wheel, right: Wheel): DifferentialWheelMotor =
    DifferentialWheelMotorImpl(left, right)

  /**
   * Implementation of the [[DifferentialWheelMotor]] trait that uses differential drive to move the robot.
   *
   * @param left
   *   the left wheel of the motor.
   * @param right
   *   the right wheel of the motor.
   */
  private case class DifferentialWheelMotorImpl(left: Wheel, right: Wheel) extends DifferentialWheelMotor:

    /**
     * Computes the updated position and orientation of a differential-drive robot based on the speeds of its wheels and
     * the time interval `dt`.
     *
     * The robot is assumed to move on a 2D plane, and the orientation is in radians.
     *
     * @param dt
     *   the time delta for which the robot is moving.
     * @param robot
     *   the robot whose state should be updated.
     * @return
     *   a new [[Robot]] instance with updated position and orientation.
     */
    override def act[F[_]: Monad](dt: FiniteDuration, robot: Robot): F[Robot] =
      import DifferentialKinematics.*

      val wheelDistance = robot.shape.radius * 2
      val theta = robot.orientation.toRadians

      val updatedRobot = (
        computeWheelVelocities
          andThen computeVelocities(wheelDistance)
          andThen computePositionAndOrientation(theta, dt)
          andThen { case (dx, dy, newOrientation) =>
            val newPos = Point2D(robot.position.x + dx, robot.position.y + dy)
            (robot at newPos withOrientation newOrientation).validate
          }
      )(this)
      Monad[F].pure(updatedRobot.getOrElse(robot))

  end DifferentialWheelMotorImpl

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
    def move[F[_]: Monad](dt: FiniteDuration): F[Robot] =
      robot.actuators.collectFirst { case wm: DifferentialWheelMotor => wm } match
        case Some(wm) => wm.act[F](dt, robot)
        case None => Monad[F].pure(robot)

    /**
     * Applies a list of actions to the robot, updating its state accordingly.
     *
     * @param actions
     *   the list of [[MovementAction]] to apply to the robot.
     * @return
     *   the robot with updated state after applying the actions.
     */
    def applyMovementActions[F[_]: Monad](dt: FiniteDuration, action: Action[F])(using ra: RobotAction[F]): F[Robot] =
//      actions.foldLeftM(robot)((r, a) => a.run(r).flatMap(_.move[F](dt)))
      action.run(robot).flatMap(_.move(dt))
  end extension
end DifferentialWheelMotor
