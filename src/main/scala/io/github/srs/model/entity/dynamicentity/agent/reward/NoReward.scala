package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment

class NoReward extends RewardModel[Agent]:

  override def evaluate(
      prev: Environment,
      current: Environment,
      entity: Agent,
      action: Action[?],
  ): Double = 0.0