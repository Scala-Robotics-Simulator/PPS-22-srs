package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.entity.Point2D.*

/**
 * Generic termination model for goal-reaching tasks.
 *
 * Episode terminates (SUCCESS) when the agent successfully reaches a goal (light source). This is reusable for any task
 * where reaching a target location indicates success: phototaxis, waypoint navigation, target seeking, etc.
 */
final case class LightReached() extends TerminationModel[Agent]:
  private val GoalReachedDistance = 0.5 // Distance threshold to consider light "reached"

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    val lights = current.environment.entities.collect { case l: Light => l }

    if lights.isEmpty then false // No lights to reach, episode continues
    else
      val nearestLightDistance =
        lights.map(light => entity.position.distanceTo(light.position)).minOption.getOrElse(Double.MaxValue)
      nearestLightDistance < GoalReachedDistance // TRUE = SUCCESS, episode ends
