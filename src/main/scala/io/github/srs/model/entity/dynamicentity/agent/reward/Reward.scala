package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * An enumeration of available reward models for agents.
 *
 * This enum represents the different types of reward models that can be applied to agents in the simulation. Each case
 * corresponds to a specific reward calculation strategy.
 */
enum Reward(val name: String) derives CanEqual:
  case NoReward extends Reward("NoReward")

  /**
   * Converts the enum case to its corresponding [[RewardModel]] instance.
   *
   * @return
   *   the [[RewardModel]] implementation for this reward type
   */
  def toRewardModel: RewardModel[Agent] =
    this match
      case NoReward => io.github.srs.model.entity.dynamicentity.agent.reward.NoReward()

  /**
   * String representation of the reward type.
   * @return
   *   name of the reward
   */
  override def toString: String = name
