package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.ModelModule.BaseState

/**
 * Default placeholder reward model. Returns zero for every transition — suitable as a neutral fallback.
 */
final case class NoReward() extends RewardModel[Agent]:

  override def evaluate(
      prev: BaseState,
      current: BaseState,
      entity: Agent,
      action: Action[?],
  ): Double = 0.0
