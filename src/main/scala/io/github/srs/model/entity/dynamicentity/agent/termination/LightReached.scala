package io.github.srs.model.entity.dynamicentity.agent.termination

import com.typesafe.scalalogging.Logger
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.util.TerminationUtils

final case class LightReached() extends TerminationModel[Agent]:
  private val logger = Logger(this.getClass)

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    val updatedAgent = current.environment.entities.collectFirst { case a: Agent if a.id.equals(entity.id) => a }
      .getOrElse(entity)

    val reached = TerminationUtils.atLeastOneLightReached(updatedAgent, current.environment)
    if reached then logger.info(f"[LightReached] SUCCESS: Agent ${entity.id} reached the goal.")
    reached
