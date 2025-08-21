package io.github.srs.model.logic

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import cats.effect.IO
import cats.syntax.parallel.catsSyntaxParallelTraverse1
import io.github.srs.controller.message.RobotProposal
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.EqualityGivenInstances.given

trait RobotActionsLogic[S <: ModelModule.State]:

  def handleRobotActionsProposals(
      s: S,
      proposals: List[RobotProposal],
  ): IO[S]

object RobotActionsLogic:

  given RobotActionsLogic[SimulationState] with

    def handleRobotActionsProposals(
        s: SimulationState,
        proposals: List[RobotProposal],
    ): IO[SimulationState] =

      def safeMove(
          env: ValidEnvironment,
          robot: Robot,
          action: Action[IO],
          maxDt: FiniteDuration,
          maxAttempts: Int = 50,
      ): IO[Robot] =

        def binarySearch(low: FiniteDuration, high: FiniteDuration, best: Option[Robot], attempts: Int): IO[Robot] =
          if attempts >= maxAttempts || (high - low) <= 1.microsecond then IO.pure(best.getOrElse(robot))
          else
            val mid = low + (high - low) / 2
            robot.applyMovementActions[IO](mid, action).flatMap { candidate =>
              val updatedEntities = env.entities.map:
                case r: Robot if r.id == robot.id => candidate
                case e => e
              env.copy(entities = updatedEntities).validate match
                case Right(_) => binarySearch(mid, high, Some(candidate), attempts + 1)
                case Left(_) => binarySearch(low, mid, best, attempts + 1)
            }

        binarySearch(0.nanos, maxDt, None, 0)
      end safeMove

      def computeMovesParallel(env: ValidEnvironment, proposals: List[RobotProposal]): IO[List[(Robot, Robot)]] =
        proposals.parTraverse:
          case RobotProposal(robot, action) =>
            safeMove(env, robot, action, s.dt).map(updatedRobot => (robot, updatedRobot))

      def applyAllMoves(env: ValidEnvironment, moves: List[(Robot, Robot)]): Environment =
        env.copy(entities = env.entities.map {
          case r: Robot =>
            moves.collectFirst { case (orig, updated) if r.id == orig.id => updated }.getOrElse(r)
          case e => e
        })

      for
        moves <- computeMovesParallel(s.environment, proposals)

        finalEnv <- IO:
          val candidate = applyAllMoves(s.environment, moves)
          candidate.validate match
            case Right(validEnv) =>
              moves.foreach { case (_, newR) =>
                println(s"[${newR.position._1}, ${newR.position._2}],")
              }
              validEnv
            case Left(_) =>
              s.environment
      yield s.copy(environment = finalEnv)
    end handleRobotActionsProposals
  end given
end RobotActionsLogic
