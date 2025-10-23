package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.environment.Environment
import io.github.srs.model.entity.dynamicentity.agent.reward.ObstacleAvoidance as ObstacleAvoidanceRewardModel

/**
 * An enumeration of available reward models for agents.
 *
 * This enum represents the different types of reward models that can be applied to agents in the simulation. Each case
 * corresponds to a specific reward calculation strategy.
 */
enum Reward(val name: String) derives CanEqual:
  case NoReward extends Reward("NoReward")
  case ObstacleAvoidance extends Reward("ObstacleAvoidance")

  /**
   * Evaluates the reward for the given state transition and action.
   *
   * @param prev
   *   the previous environment state
   * @param current
   *   the current environment state
   * @param entity
   *   the agent being evaluated
   * @param action
   *   the action that was taken
   * @return
   *   the reward value for this transition
   */
  def evaluate(prev: Environment, current: Environment, entity: Agent, action: Action[?]): Double =
    this match
      case NoReward => 0.0
      case ObstacleAvoidance => ObstacleAvoidanceRewardModel().evaluate(prev, current, entity, action)

  /**
   * String representation of the reward type.
   * @return
   *   name of the reward
   */
  override def toString: String = name

end Reward
