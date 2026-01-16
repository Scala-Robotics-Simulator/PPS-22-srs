package io.github.srs.model.entity.dynamicentity.agent.reward

import cats.Id
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.util.TerminationUtils
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

  private val NoLightThreshold = 0.01

  final case class PhototaxisQ() extends RewardModel[Agent]:
    private val ProgressScale = 2.0
    private val StepPenalty = -0.1
    private val GoalProximityRadius = 1.5
    private val TractorBeamScale = 2.0
    private val GoalBonus = 800.0
    private val FailurePenalty = -350.0

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
        val collided = TerminationUtils.isCollided(agentNow, current)
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

            if currDist < GoalProximityRadius then
              val attractorEffect = (GoalProximityRadius - currDist) * TractorBeamScale
              attractorEffect + StepPenalty
            else
              val progress = prevDist - currDist
              val rProgress = if progress.isNaN then 0.0 else ProgressScale * progress
              rProgress + StepPenalty
        end if
      end if
    end evaluate
  end PhototaxisQ

  final case class PhototaxisDQ() extends RewardModel[Agent]:

    private val ProgressScale = 5.0

    // incentive
    private val StepPenalty = -0.004

    // darkness penalty
    private val DarknessPenalty = -0.006

    // anti-stuck mechanism
    private val StationaryPenalty = -0.025
    private val StationaryThreshold = 0.01

    // terminal rewards
    private val GoalBonus = 1.5
    private val FailurePenalty = -2.0

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
        val collided = TerminationUtils.isCollided(agentNow, current)
        if collided then FailurePenalty
        else
          val agentPrev = getAgent(prev.environment, entity)
          val positionChange = agentPrev.position.distanceTo(agentNow.position)

          val movementPenalty =
            if positionChange < StationaryThreshold then StationaryPenalty
            else 0.0

          val maxLight = agentNow
            .senseAll[Id](current.environment)
            .lightReadings
            .map(_.value)
            .foldLeft(0.0)(math.max)

          if maxLight < NoLightThreshold then DarknessPenalty + StepPenalty + movementPenalty
          else
            val prevDist = distanceToNearestLight(prev.environment, agentPrev)
            val currDist = distanceToNearestLight(current.environment, agentNow)
            val progress = prevDist - currDist

            val rProgress =
              if progress.isNaN then 0.0
              else ProgressScale * progress

            rProgress + StepPenalty + movementPenalty
        end if

      end if
    end evaluate
  end PhototaxisDQ

end PhototaxisReward
