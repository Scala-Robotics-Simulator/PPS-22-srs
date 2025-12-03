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
 * Multiple Phototaxis Reward Variants for Experimental Comparison
 *
 * This module contains 5 different reward function variants to test which parameters lead to the best learning
 * performance in phototaxis tasks.
 */
object PhototaxisRewardVariants:
  import io.github.srs.model.entity.Point2D.distanceTo

  private def getAgent(env: Environment, entity: Agent): Agent =
    env.entities.collectFirst {
      case a: Agent if a.id.equals(entity.id) => a
    }.getOrElse(entity)

  private def distanceToNearestLight(env: Environment, agent: Agent): Double =
    env.entities.collect { case l: Light => l }
      .foldLeft(Double.MaxValue)((minDist, light) => math.min(minDist, agent.position.distanceTo(light.position)))

  private val NoLightThreshold = 0.01

  /**
   * Variant 1: BASELINE (Current Default)
   *   - Moderate progress rewards
   *   - Standard tractor beam
   *   - Balanced penalties
   */
  final case class PhototaxisV1() extends RewardModel[Agent]:
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
  end PhototaxisV1

  /**
   * Variant 2: STRONG PROGRESS
   *   - 4x increased progress scale (guiding towards light)
   *   - Reduced step penalty (encourage exploration)
   *   - Same tractor beam
   */
  final case class PhototaxisV2() extends RewardModel[Agent]:
    private val ProgressScale = 8.0 // 4x increase
    private val StepPenalty = -0.05 // 50% reduction
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
  end PhototaxisV2

  /**
   * Variant 3: POWERFUL TRACTOR BEAM
   *   - Strong attraction near goal (7.5x)
   *   - Larger proximity radius (more help near goal)
   *   - Moderate progress scale
   */
  final case class PhototaxisV3() extends RewardModel[Agent]:
    private val ProgressScale = 5.0
    private val StepPenalty = -0.1
    private val GoalProximityRadius = 2.5 // Increased from 1.5
    private val TractorBeamScale = 15.0 // 7.5x increase
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
  end PhototaxisV3

  final case class PhototaxisV4() extends RewardModel[Agent]:
    private val ProgressScale = 5.0
    private val StepPenalty = -0.05
    private val GoalProximityRadius = 2.0
    private val TractorBeamScale = 8.0
    private val GoalBonus = 600.0 // Reduced
    private val FailurePenalty = -200.0 // Less harsh
    private val LightDetectionBonus = 1.0 // NEW: reward for seeing light

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

          if maxLight < NoLightThreshold then StepPenalty // Just step penalty
          else
            val agentPrev = getAgent(prev.environment, entity)
            val prevDist = distanceToNearestLight(prev.environment, agentPrev)
            val currDist = distanceToNearestLight(current.environment, agentNow)

            val curiosityBonus = LightDetectionBonus * maxLight // Scale with light intensity

            if currDist < GoalProximityRadius then
              val attractorEffect = (GoalProximityRadius - currDist) * TractorBeamScale
              attractorEffect + curiosityBonus + StepPenalty
            else
              val progress = prevDist - currDist
              val rProgress = if progress.isNaN then 0.0 else ProgressScale * progress
              rProgress + curiosityBonus + StepPenalty
        end if
      end if
    end evaluate
  end PhototaxisV4

  /**
   * Variant 5: BALANCED AGGRESSIVE (problem with farming)
   *   - Strong progress AND strong tractor beam
   *   - Larger goal radius
   *   - Moderate penalties Best of V2 and V3 combined
   */
  final case class PhototaxisV5() extends RewardModel[Agent]:
    private val ProgressScale = 8.0 // From V2
    private val StepPenalty = -0.07 // Compromise
    private val GoalProximityRadius = 2.5 // From V3
    private val TractorBeamScale = 15.0 // From V3
    private val GoalBonus = 700.0 // Moderate
    private val FailurePenalty = -250.0 // Moderate

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
  end PhototaxisV5

end PhototaxisRewardVariants
