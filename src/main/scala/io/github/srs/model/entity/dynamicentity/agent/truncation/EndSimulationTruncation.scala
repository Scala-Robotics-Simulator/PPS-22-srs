package io.github.srs.model.entity.dynamicentity.agent.truncation

import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * Default placeholder termination model. Returns false for every transition — suitable as a neutral fallback.
 */
final case class EndSimulationTruncation() extends TruncationModel[Agent]:

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    current.simulationTime match
      case None => false
      case Some(duration) => current.elapsedTime >= duration
