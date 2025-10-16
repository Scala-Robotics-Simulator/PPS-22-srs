package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment

final class RewardPhototaxis extends RewardModel[Agent]:

  override def evaluate(
      prev: Environment,
      curr: Environment,
      agent: Agent,
      action: Action[?],
  ): Double =
    val maybeLightPos = curr.entities.collectFirst { case StaticEntity.Light(_, pos, _, _, _, _, _) => pos }
    maybeLightPos match
      case Some(_, _) => 1.0
      case _ => 0.0
