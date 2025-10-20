package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment

/**
 * Default placeholder reward model. Returns zero for every transition — suitable as a neutral fallback.
 */
class NoReward extends RewardModel[Agent]:

  override def evaluate(
      prev: Environment,
      current: Environment,
      entity: Agent,
      action: Action[?],
  ): Double = 0.0
