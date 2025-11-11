package io.github.srs.model.logic

import java.util.UUID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import cats.effect.IO
import cats.implicits.*
import com.typesafe.scalalogging.Logger
import io.github.srs.controller.message.DynamicEntityProposal
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.entity.dynamicentity.actuator.{ given_Kinematics_DynamicEntity, DifferentialWheelMotor }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.EqualityGivenInstances.given
import io.github.srs.utils.SimulationDefaults.DynamicEntity.DefaultMaxRetries
import io.github.srs.utils.SimulationDefaults.{ BinarySearchDurationThreshold, DebugMode }
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.BaseSimulationState
import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * Logic for handling dynamic entities actions within the simulation.
 *
 * @tparam S
 *   the type of the simulation state.
 */
trait DynamicEntityActionsLogic[S <: ModelModule.BaseState]:

  /**
   * Handles motion for *autonomous* dynamic entities (i.e. robots with internal policy, or autonomous agents).
   * @param s
   *   the current simulation state.
   * @param proposals
   *   the list of dynamic entity action proposals.
   * @return
   *   an [[cats.effect.IO]] effect that produces the updated simulation state.
   */
  def handleDynamicEntityActionsProposals(
      s: S,
      proposals: List[DynamicEntityProposal],
  ): IO[S]

/**
 * Companion object for [[DynamicEntityActionsLogic]] containing given instances.
 */
object DynamicEntityActionsLogic:

  private val logger = Logger(getClass.getName)

  private def computeNextEnvironment(
      s: ModelModule.BaseState,
      proposals: List[DynamicEntityProposal],
  ): IO[ValidEnvironment] =
    /**
     * Safely moves a dynamic entity based on the given action, ensuring no collisions occur. Uses a binary search
     * approach to find the maximum safe movement duration.
     * @param env
     *   the valid environment in which the dynamic entities operate.
     * @param entity
     *   the dynamic entity to be moved.
     * @param action
     *   the action to be applied to the dynamic entity.
     * @param maxDt
     *   the maximum duration for which the action can be applied.
     * @param maxAttempts
     *   the maximum number of attempts for the binary search.
     * @return
     *   an [[cats.effect.IO]] effect that produces the updated dynamic entity after applying the safe movement.
     */
    def safeMove(
        env: ValidEnvironment,
        entity: DynamicEntity,
        action: Action[cats.effect.IO],
        maxDt: FiniteDuration,
        maxAttempts: Int = DefaultMaxRetries,
    ): IO[DynamicEntity] =

      /**
       * Performs a binary search to find the maximum safe movement duration.
       * @param low
       *   the lower bound of the search interval.
       * @param high
       *   the upper bound of the search interval.
       * @param best
       *   the best valid dynamic entity found so far.
       * @param attempts
       *   the current number of attempts made.
       * @return
       *   an [[cats.effect.IO]] effect that produces the best valid dynamic entity found.
       */
      def binarySearch(
          low: FiniteDuration,
          high: FiniteDuration,
          best: Option[DynamicEntity],
          attempts: Int,
      ): IO[DynamicEntity] =
        if attempts >= maxAttempts || (high - low) <= BinarySearchDurationThreshold then IO.pure(best.getOrElse(entity))
        else
          val mid = low + (high - low) / 2
          val maybeMotor = entity.actuators.collectFirst:
            case m: DifferentialWheelMotor[DynamicEntity] @unchecked => m

          maybeMotor match
            case None =>
              IO.pure(entity) // no movement possible

            case Some(motor) =>
              motor
                .applyMovementActions(entity, mid, action)
                .flatMap { candidate =>
                  env.updateEntity(candidate) match
                    case Right(_) => binarySearch(mid, high, Some(candidate), attempts + 1)
                    case Left(_) =>
                      binarySearch(
                        low,
                        mid,
                        best.map {
                          case a: Agent => a.copy(didCollide = true)
                          case e: DynamicEntity => e
                        },
                        attempts + 1,
                      )
                }
          end match

      binarySearch(0.nanos, maxDt, None, 0)
    end safeMove

    /**
     * Computes the moves for all dynamic entity proposals.
     * @param env
     *   the valid environment.
     * @param proposals
     *   the list of dynamic action proposals.
     * @return
     *   an [[cats.effect.IO]] effect that produces a list of tuples containing the original and updated dynamic
     *   entities.
     */
    def computeMoves(
        env: ValidEnvironment,
        proposals: List[DynamicEntityProposal],
        dt: FiniteDuration,
    ): IO[List[(DynamicEntity, DynamicEntity)]] =
      proposals
        .sortBy(_.entity.id.toString)
        .traverse:
          case DynamicEntityProposal(entity, action) =>
            safeMove(env, entity, action, dt).map(updatedEntity => (entity, updatedEntity))

    /**
     * Applies all computed moves to the environment.
     * @param env
     *   the valid environment.
     * @param moves
     *   the list of tuples containing the original and updated dynamic entities.
     * @return
     *   the updated environment with all moves applied.
     */
    def applyAllMoves(env: ValidEnvironment, moves: List[(DynamicEntity, DynamicEntity)]): Environment =
      val movesMap: Map[UUID, DynamicEntity] = moves.map { case (orig, updated) => orig.id -> updated }.toMap
      val updatedEntities = env.entities.map:
        case e: DynamicEntity => movesMap.getOrElse(e.id, e)
        case e => e
      env.copy(entities = updatedEntities)
    for
      moves <- computeMoves(s.environment, proposals, s.dt)

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
    yield finalEnv

  end computeNextEnvironment

  given DynamicEntityActionsLogic[SimulationState] with

    def handleDynamicEntityActionsProposals(
        s: SimulationState,
        proposals: List[DynamicEntityProposal],
    ): IO[SimulationState] =
      for finalEnv <- computeNextEnvironment(s, proposals)
      yield s.copy(environment = finalEnv)

  given DynamicEntityActionsLogic[BaseSimulationState] with

    def handleDynamicEntityActionsProposals(
        s: BaseSimulationState,
        proposals: List[DynamicEntityProposal],
    ): IO[BaseSimulationState] =
      for finalEnv <- computeNextEnvironment(s, proposals) yield s.copy(environment = finalEnv)
end DynamicEntityActionsLogic
