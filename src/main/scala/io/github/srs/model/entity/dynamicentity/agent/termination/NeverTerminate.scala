package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.ModelModule.BaseState

/**
 * Default placeholder termination model. Returns false for every transition — suitable as a neutral fallback.
 */
final case class NeverTerminate() extends TerminationModel[Agent]:
  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean = false
