package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.{ exp, min }

import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.environment.Environment
import cats.Id
import com.typesafe.scalalogging.Logger
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.MovementAction

object ObstacleAvoidanceRewardModule:
  private var ticks: Int = 0
  private val maxTicks: Int = 10_000
  private var positions: List[Point2D] = List()
  private var actionHistory: List[Action[?]] = List()
  private val actionHistorySize: Int = 10

  /**
   * Reward model focused on obstacle avoidance.
   */
  final case class ObstacleAvoidance() extends RewardModel[Agent]:
    private val logger = Logger(getClass.getName)

    override def evaluate(
        prev: Environment,
        current: Environment,
        entity: Agent,
        action: Action[?],
    ): Double =
      if entity.aliveSteps == 1 then restoreState(entity)

      // Update action history (circular buffer)
      actionHistory = (actionHistory ::: List(action)).takeRight(actionHistorySize)

      val currentMin = distanceFromObstacle(current, entity)
      val rExpl = explorationReward(entity) * 5
      val rSurv = survivalReward(ticks, maxTicks)
      val rClear = clearanceReward(prev, current, entity)
      val rColl = if currentMin < CollisionTriggerDistance then -100.0 else 0.0
      val rOscill = oscillationPenalty()

      logger.info(s"evaluation at tick: $ticks")
      logger.info(s"exploration reward: $rExpl")
      logger.info(s"survival reward: $rSurv")
      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      logger.info(s"oscillation penalty: $rOscill")

      val reward = rExpl + rSurv + rClear + rColl + rOscill
      logger.info(s"total reward: $reward")

      ticks += 1
      reward
    end evaluate

    private def oscillationPenalty(): Double =
      if actionHistory.sizeIs < 6 then 0.0
      else

        // Split history into two halves and compute average velocities
        val halfSize = actionHistory.size / 2
        val firstHalf = actionHistory.take(halfSize)
        val secondHalf = actionHistory.drop(halfSize)

        val (avgLeft1, avgRight1) = computeAverageVelocities(firstHalf)
        val (avgLeft2, avgRight2) = computeAverageVelocities(secondHalf)

        // Check if the average velocities indicate opposite movements
        val leftOscillation = if (avgLeft1 * avgLeft2) < 0 then 1.0 else 0.0
        val rightOscillation = if (avgRight1 * avgRight2) < 0 then 1.0 else 0.0

        val oscillationScore = (leftOscillation + rightOscillation) / 2.0

        logger.info(s"avgLeft1: $avgLeft1, avgRight1: $avgRight1")
        logger.info(s"avgLeft2: $avgLeft2, avgRight2: $avgRight2")
        logger.info(s"oscillation score: $oscillationScore")

        // Use exponential to make penalty grow aggressively
        // When oscillationScore = 0: penalty = 0
        // When oscillationScore = 0.5: penalty ≈ -0.65
        // When oscillationScore = 1.0: penalty ≈ -1.72
        val penalty = -(exp(oscillationScore * 2) - 1)
        logger.info(s"oscillation penalty (exponential): $penalty")

        penalty

    end oscillationPenalty

    private def computeAverageVelocities(actions: List[Action[?]]): (Double, Double) =
      val movements = actions.collect { case ma: MovementAction[?] => ma }
      if movements.isEmpty then (0.0, 0.0)
      else

        val totalLeft = movements.map(_.leftSpeed).sum
        val totalRight = movements.map(_.rightSpeed).sum

        (totalLeft / movements.size, totalRight / movements.size)

    private def clearanceReward(prev: Environment, current: Environment, entity: Agent): Double =
      val prevMin = distanceFromObstacle(prev, entity)
      val currMin = distanceFromObstacle(current, entity)
      val delta = currMin - prevMin
      val rChange = delta * 10.0
      val rProximity = -exp(-5 * currMin)
      rChange + rProximity

    private def restoreState(entity: Agent): Unit =
      logger.info("restoring obstacle avoidance reward state")
      ticks = 0
      positions = List(entity.position)
      actionHistory = List()

    private def explorationReward(entity: Agent): Double =
      val current = entity.position
      val displacement = positions.map(current.distanceTo).minOption.getOrElse(0.0)
      if ticks % 100 == 0 then positions = positions ::: List(current)
      displacement

    private def survivalReward(duration: Int, maxDuration: Int): Double =
      val t = duration.toDouble.max(1.0)
      val K = 0.1
      val norm = math.log(1.0 + maxDuration)
      val totalSoFar = K * math.log(1.0 + t) / norm
      val totalPrev = K * math.log(1.0 + (t - 1.0)) / norm
      val rSurvStep = totalSoFar - totalPrev
      rSurvStep

    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      val agent =
        env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
      agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

  end ObstacleAvoidance
end ObstacleAvoidanceRewardModule
