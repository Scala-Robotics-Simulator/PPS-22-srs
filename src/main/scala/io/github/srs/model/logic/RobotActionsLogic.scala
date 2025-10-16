package io.github.srs.model.logic

import java.util.UUID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import cats.effect.IO
import cats.implicits.*
import com.typesafe.scalalogging.Logger
import io.github.srs.controller.message.RobotProposal
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.entity.dynamicentity.actuator.{ given_Kinematics_Robot, DifferentialWheelMotor }
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.EqualityGivenInstances.given
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.DefaultMaxRetries
import io.github.srs.utils.SimulationDefaults.{ BinarySearchDurationThreshold, DebugMode }

/**
 * Logic for handling robot actions within the simulation.
 *
 * @tparam S
 *   the type of the simulation state.
 */
trait RobotActionsLogic[S <: ModelModule.BaseState]:

  /**
   * Handles motion for *autonomous* dynamic entities (i.e. robots with internal policy).
   * @param s
   *   the current simulation state.
   * @param proposals
   *   the list of robot action proposals.
   * @return
   *   an [[cats.effect.IO]] effect that produces the updated simulation state.
   */
  def handleRobotActionsProposals(
      s: S,
      proposals: List[RobotProposal],
  ): IO[S]

/**
 * Companion object for [[RobotActionsLogic]] containing given instances.
 */
object RobotActionsLogic:

  private val logger = Logger(getClass.getName)

  given RobotActionsLogic[SimulationState] with

    def handleRobotActionsProposals(
        s: SimulationState,
        proposals: List[RobotProposal],
    ): IO[SimulationState] =

      /**
       * Safely moves a robot based on the given action, ensuring no collisions occur. Uses a binary search approach to
       * find the maximum safe movement duration.
       * @param env
       *   the valid environment in which the robot operates.
       * @param robot
       *   the robot to be moved.
       * @param action
       *   the action to be applied to the robot.
       * @param maxDt
       *   the maximum duration for which the action can be applied.
       * @param maxAttempts
       *   the maximum number of attempts for the binary search.
       * @return
       *   an [[cats.effect.IO]] effect that produces the updated robot after applying the safe movement.
       */
      def safeMove(
          env: ValidEnvironment,
          robot: Robot,
          action: Action[cats.effect.IO],
          maxDt: FiniteDuration,
          maxAttempts: Int = DefaultMaxRetries,
      ): IO[Robot] =

        /**
         * Performs a binary search to find the maximum safe movement duration.
         * @param low
         *   the lower bound of the search interval.
         * @param high
         *   the upper bound of the search interval.
         * @param best
         *   the best valid robot found so far.
         * @param attempts
         *   the current number of attempts made.
         * @return
         *   an [[cats.effect.IO]] effect that produces the best valid robot found.
         */
        def binarySearch(low: FiniteDuration, high: FiniteDuration, best: Option[Robot], attempts: Int): IO[Robot] =
          if attempts >= maxAttempts || (high - low) <= BinarySearchDurationThreshold then
            IO.pure(best.getOrElse(robot))
          else
            val mid = low + (high - low) / 2
            val maybeMotor = robot.actuators.collectFirst:
              case m: DifferentialWheelMotor[Robot] => m

            maybeMotor match
              case None =>
                IO.pure(robot) // no movement possible

              case Some(motor) =>
                motor
                  .applyMovementActions(robot, mid, action)
                  .flatMap { candidate =>
                    env.updateEntity(candidate) match
                      case Right(_) => binarySearch(mid, high, Some(candidate), attempts + 1)
                      case Left(_) => binarySearch(low, mid, best, attempts + 1)
                  }

        binarySearch(0.nanos, maxDt, None, 0)
      end safeMove

      /**
       * Computes the moves for all robot proposals.
       * @param env
       *   the valid environment.
       * @param proposals
       *   the list of robot action proposals.
       * @return
       *   an [[cats.effect.IO]] effect that produces a list of tuples containing the original and updated robots.
       */
      def computeMoves(env: ValidEnvironment, proposals: List[RobotProposal]): IO[List[(Robot, Robot)]] =
        proposals
          .sortBy(_.robot.id.toString)
          .traverse:
            case RobotProposal(robot, action) =>
              safeMove(env, robot, action, s.dt).map(updatedRobot => (robot, updatedRobot))

      /**
       * Applies all computed moves to the environment.
       * @param env
       *   the valid environment.
       * @param moves
       *   the list of tuples containing the original and updated robots.
       * @return
       *   the updated environment with all moves applied.
       */
      def applyAllMoves(env: ValidEnvironment, moves: List[(Robot, Robot)]): Environment =
        val movesMap: Map[UUID, Robot] = moves.map { case (orig, updated) => orig.id -> updated }.toMap
        val updatedEntities = env.entities.map:
          case r: Robot => movesMap.getOrElse(r.id, r)
          case e => e
        env.copy(entities = updatedEntities)

      for
        moves <- computeMoves(s.environment, proposals)

        finalEnv <- IO:
          val candidate = applyAllMoves(s.environment, moves)
          candidate.validate match
            case Right(validEnv) =>
              if DebugMode then
                moves.foreach { case (_, newR) =>
                  logger.debug(s"[${newR.position._1}, ${newR.position._2}],")
                }
              validEnv
            case Left(_) =>
              s.environment
      yield s.copy(environment = finalEnv)
    end handleRobotActionsProposals
  end given
end RobotActionsLogic
