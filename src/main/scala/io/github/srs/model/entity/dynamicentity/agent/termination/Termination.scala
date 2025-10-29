package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.entity.dynamicentity.agent.termination.LightReached as LightReachedTerminationModel
import io.github.srs.model.entity.dynamicentity.agent.termination.CollisionDetection as CollisionDetectionTerminationModel
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.ModelModule.BaseState

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
      case LightReached => LightReachedTerminationModel().evaluate(prev, current, entity, action)
      case CollisionDetection => CollisionDetectionTerminationModel().evaluate(prev, current, entity, action)

  /**
   * String representation of the termination type.
   * @return
   *   name of the termination
   */
  override def toString: String = name

end Termination
