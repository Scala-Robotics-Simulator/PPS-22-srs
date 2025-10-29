package io.github.srs.model.entity.dynamicentity.agent.truncation

import io.github.srs.model.entity.dynamicentity.agent.truncation.CollisionDetection as CollisionDetectionTruncationModel
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.ModelModule.BaseState

/**
 * An enumeration of available truncation models for agents.
 *
 * This enum represents the different types of truncation models that can be applied to agents in the simulation. Each
 * case corresponds to a specific truncation evaluation strategy.
 */
enum Truncation(val name: String) derives CanEqual:
  case NeverTruncate extends Truncation("NeverTruncate")
  case CollisionDetection extends Truncation("CollisionDetection")

  /**
   * Evaluates whether the episode should be truncated based on the state transition and action.
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
   *   true if the episode should be truncated, false otherwise
   */
  def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    this match
      case NeverTruncate => false
      case CollisionDetection => CollisionDetectionTruncationModel().evaluate(prev, current, entity, action)

  /**
   * String representation of the truncation type.
   * @return
   *   name of the truncation
   */
  override def toString: String = name

end Truncation
