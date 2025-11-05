package io.github.srs.model.entity.dynamicentity.agent.truncation

import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * Generic termination model for collision detection.
 *
 * Episode termination (FAILURE) when the agent collides with an obstacle (proximity sensor < threshold). This is
 * reusable for any task where collision indicates failure: phototaxis, obstacle avoidance, navigation, safe
 * exploration, etc.
 */
final case class CollisionDetection() extends TerminationModel[Agent]:

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    entity.didCollide
