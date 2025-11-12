package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.reward.ObstacleAvoidanceRewardModule
import io.github.srs.model.entity.dynamicentity.agent.reward.PhototaxisRewardModule
import io.github.srs.model.ModelModule.BaseState

/**
 * An enumeration of available reward models for agents.
 *
 * This enum represents the different types of reward models that can be applied to agents in the simulation. Each case
 * corresponds to a specific reward calculation strategy.
 */
enum Reward(val name: String) derives CanEqual:
  case NoReward extends Reward("NoReward")
  case QObstacleAvoidance extends Reward("QObstacleAvoidance")
  case DQObstacleAvoidance extends Reward("DQObstacleAvoidance")
  case Phototaxis extends Reward("Phototaxis")
  case ExplorationQL extends Reward("ExplorationQL")
  case ExplorationDQN extends Reward("ExplorationDQN")

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
  def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Double =
    this match
      case NoReward => 0.0
      case QObstacleAvoidance =>
        ObstacleAvoidanceRewardModule.QObstacleAvoidance().evaluate(prev, current, entity, action)
      case DQObstacleAvoidance =>
        ObstacleAvoidanceRewardModule.DQObstacleAvoidance().evaluate(prev, current, entity, action)
      case Phototaxis =>
        PhototaxisRewardModule.Phototaxis().evaluate(prev, current, entity, action)
      case ExplorationQL =>
        ExplorationReward.ExplorationQL().evaluate(prev, current, entity, action)
      case ExplorationDQN =>
        ExplorationReward.ExplorationDQN().evaluate(prev, current, entity, action)

  /**
   * String representation of the reward type.
   * @return
   *   name of the reward
   */
  override def toString: String = name

end Reward
