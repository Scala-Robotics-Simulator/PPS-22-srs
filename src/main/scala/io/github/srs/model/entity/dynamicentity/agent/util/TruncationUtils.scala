package io.github.srs.model.entity.dynamicentity.agent.util

import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.staticentity.StaticEntity

object TruncationUtils:

  private val Eps = 1e-4 // TODO: make configurable

  def isCollided(agent: Agent, current: BaseState): Boolean =
    val agentRadius = agent.shape.radius
    val pos = agent.position
    import io.github.srs.model.entity.Point2D.*
    val hitBoundary =
      pos.x <= agentRadius + Eps || pos.x >= current.environment.width.toDouble - agentRadius - Eps ||
        pos.y <= agentRadius + Eps || pos.y >= current.environment.height.toDouble - agentRadius - Eps

    val hitObstacle = current.environment.entities.collect { case o: StaticEntity.Obstacle =>
      o
    }.exists { o =>
      val hw = o.width / 2.0
      val hh = o.height / 2.0
      val dx = math.max(math.abs(pos.x - o.position.x) - hw, 0.0)
      val dy = math.max(math.abs(pos.y - o.position.y) - hh, 0.0)
      val dist2 = dx * dx + dy * dy
      val collided = dist2 <= (agentRadius + Eps) * (agentRadius + Eps)
      collided
    }
    hitBoundary || hitObstacle
  end isCollided
end TruncationUtils
