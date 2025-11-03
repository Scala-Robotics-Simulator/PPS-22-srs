package io.github.srs.model.entity.dynamicentity.agent.truncation

import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.util.TruncationUtils

final case class CollisionContact() extends TruncationModel[Agent]:

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =

    val updatedAgent = current.environment.entities
      .collectFirst:
        case a: Agent if a.id.toString == entity.id.toString => a
      .getOrElse(entity)

    TruncationUtils.isCollided(updatedAgent, current)
