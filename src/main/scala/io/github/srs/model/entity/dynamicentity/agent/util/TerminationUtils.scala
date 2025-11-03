package io.github.srs.model.entity.dynamicentity.agent.util

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.environment.Environment

object TerminationUtils:

  private val DefaultMargin = 0.09
  private val DefaultHysteresis = 0.02

  def atLeastOneLightReached(agent: Agent, env: Environment): Boolean =
    val rA = agent.shape.radius
    env.entities.collect { case l: Light => l }.exists { light =>
      isAgentAtLight(agent.position, rA, light)
    }

  private def isAgentAtLight(
      agentPos: Point2D,
      agentRadius: Double,
      light: Light,
  ): Boolean =
    import io.github.srs.model.entity.Point2D.*
    val threshold = agentRadius + light.radius + DefaultMargin + DefaultHysteresis
    val dist = agentPos.distanceTo(light.position)
    dist <= threshold
end TerminationUtils
