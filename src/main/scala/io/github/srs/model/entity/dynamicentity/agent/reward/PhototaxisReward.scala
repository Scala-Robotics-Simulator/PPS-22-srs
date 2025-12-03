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

  final case class Phototaxis() extends RewardModel[Agent]:

    // Provides clear gradient for DQN to learn direction towards light
    private val ProgressScale = 5.0

    // Efficiency incentive: LIGHT step penalty encourages exploration over conservative behavior
    private val StepPenalty = -0.05

    // Darkness penalty: MODERATE (4x step penalty) - incentivizes finding light without over-penalizing exploration
    private val DarknessPenalty = -0.2

    // Anti-stuck mechanism: 4x step penalty prevents stationary/circular motion
    private val StationaryPenalty = -0.2
    private val StationaryThreshold = 0.001

    // Terminal rewards: HIGH goal bonus (10.7x farming potential) makes reaching light dominant strategy
    // Reduced failure penalty avoids excessive collision-avoidance fear
    private val GoalBonus = 3000.0
    private val FailurePenalty = -100.0

    private val NoLightThreshold = 0.001 // Sync with Environment

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

          // Only penalize if truly stationary (prevents stuck/spinning in place)
          val movementPenalty =
            if positionChange < StationaryThreshold then StationaryPenalty
            else 0.0

          val maxLight = agentNow
            .senseAll[Id](current.environment)
            .lightReadings
            .map(_.value)
            .foldLeft(0.0)(math.max)

          if maxLight < NoLightThreshold then
            // Out of light: darkness penalty + step penalty + stationary check
            DarknessPenalty + StepPenalty + movementPenalty
          else
            // In light: reward progress towards nearest light
            // No progress (e.g., oscillating) → rProgress ≈ 0 → net negative from StepPenalty
            // Positive progress → net positive reward
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
  end Phototaxis

end PhototaxisReward
