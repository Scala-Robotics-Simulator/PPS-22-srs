package io.github.srs.model.entity.dynamicentity.agent.reward

import cats.Id
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.util.{ TerminationUtils, TruncationUtils }
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.environment.Environment

/**
 * Phototaxis Reward Module
 */
object PhototaxisReward:
  import io.github.srs.model.entity.Point2D.distanceTo

  private def getAgent(env: Environment, entity: Agent): Agent =
    env.entities.collectFirst {
      case a: Agent if a.id.equals(entity.id) => a
    }.getOrElse(entity)

  private def distanceToNearestLight(env: Environment, agent: Agent): Double =
    env.entities.collect { case l: Light => l }
      .foldLeft(Double.MaxValue)((minDist, light) => math.min(minDist, agent.position.distanceTo(light.position)))

  final case class Phototaxis() extends RewardModel[Agent]:

    private val ProgressScale = 10.0
    private val StepPenalty = -0.01
    private val GoalBonus = 800.0
    private val FailurePenalty = -500.0

    private val NoLightThreshold = 0.01 // Sync with Environment

    override def evaluate(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
    ): Double =
      val agentNow = getAgent(current.environment, entity)
      val goalReached = TerminationUtils.atLeastOneLightReached(agentNow, current.environment)
      if goalReached then GoalBonus + StepPenalty
      else
        val collided = TruncationUtils.isCollided(agentNow, current)
        if collided then FailurePenalty
        else
          val maxLight = agentNow
            .senseAll[Id](current.environment)
            .lightReadings
            .map(_.value)
            .foldLeft(0.0)(math.max)

          if maxLight < NoLightThreshold then 0.0
          else
            val agentPrev = getAgent(prev.environment, entity)
            val prevDist = distanceToNearestLight(prev.environment, agentPrev)
            val currDist = distanceToNearestLight(current.environment, agentNow)
            val progress = prevDist - currDist

            val rProgress =
              if progress.isNaN then 0.0
              else ProgressScale * progress
            rProgress + StepPenalty
      end if
    end evaluate
  end Phototaxis

end PhototaxisReward
