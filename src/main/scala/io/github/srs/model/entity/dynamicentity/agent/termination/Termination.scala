package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.termination.EndSimulationTermination as EndSimulationTerminationModel
import io.github.srs.model.entity.dynamicentity.agent.termination.CrashedOrReached as CrashOrReachedTerminationModel
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.termination.{
  CollisionDetection as CollisionDetectionTerminationModel,
  CoverageTermination as CoverageTerminationModel,
  LightReached as LightReachedTerminationModel,
}

/**
 * An enumeration of available termination models for agents.
 *
 * This enum represents the different types of termination models that can be applied to agents in the simulation. Each
 * case corresponds to a specific termination evaluation strategy.
 */
enum Termination(val name: String) derives CanEqual:
  case NeverTerminate extends Termination("NeverTerminate")
  case LightReached extends Termination("LightReached")
  case CollisionDetection extends Termination("CollisionDetection")
  case CoverageTermination extends Termination("CoverageTermination")
  case ExplorationTermination extends Termination("ExplorationTermination")
  case EndSimulationTermination extends Termination("EndSimulationTermination")
  case CrashOrReached extends Termination("CrashOrReached")

  /**
   * Evaluates whether the agent should terminate based on the state transition and action.
   *
   * @param prev
   *   the previous state
   * @param current
   *   the current state
   * @param entity
   *   the agent being evaluated
   * @param action
   *   the action that was taken
   * @return
   *   true if the agent should terminate, false otherwise
   */
  def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    this match
      case NeverTerminate => false
      case EndSimulationTermination => EndSimulationTerminationModel().evaluate(prev, current, entity, action)
      case CrashOrReached => CrashOrReachedTerminationModel().evaluate(prev, current, entity, action)
      case CoverageTermination => coverage(prev, current, entity, action)
      case LightReached => lightReached(prev, current, entity, action)
      case CollisionDetection => collision(prev, current, entity, action)
      case ExplorationTermination =>
        collision(prev, current, entity, action) || coverage(prev, current, entity, action)

  private def coverage(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    CoverageTerminationModel().evaluate(prev, current, entity, action)

  private def collision(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    CollisionDetectionTerminationModel().evaluate(prev, current, entity, action)

  private def lightReached(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    LightReachedTerminationModel().evaluate(prev, current, entity, action)

  /**
   * String representation of the termination type.
   * @return
   *   name of the termination
   */
  override def toString: String = name

end Termination
