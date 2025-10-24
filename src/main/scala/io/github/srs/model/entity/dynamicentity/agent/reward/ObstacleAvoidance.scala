package io.github.srs.model.entity.dynamicentity.agent.reward

import scala.math.{ min, pow }

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

object ObstacleAvoidanceRewardModule:
  private var ticks: Int = 0
  private val maxTicks: Int = 10_000
  private var positions: List[Point2D] = List()

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

      val currentMin = distanceFromObstacle(current, entity)
      val rExpl = explorationReward(entity) * 10
      val rSurv = survivalReward(ticks, maxTicks)
      val rClear = pow(2, currentMin / 0.2) / 10
      val rColl = if currentMin < CollisionTriggerDistance then -100.0 else 0.0
      // val rMove = movementReward(entity.lastAction.getOrElse(NoAction[Id]()))
      logger.info(s"evaluation at tick: $ticks")
      logger.info(s"exploration reward: $rExpl")
      logger.info(s"survival reward: $rSurv")
      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      // logger.info(s"movement reward: $rMove")

      val reward = rExpl + rSurv + rClear + rColl
      logger.info(s"total reward: $reward")

      ticks += 1
      reward
    end evaluate

    private def restoreState(entity: Agent): Unit =
      logger.info("restoring obstacle avoidance reward state")
      ticks = 0
      positions = List(entity.position)

    private def explorationReward(entity: Agent): Double =
      val current = entity.position
      val displacement = positions.map(current.distanceTo).minOption.getOrElse(0.0)
      if ticks % 100 == 0 then positions = positions ::: List(current)
      displacement

    private def survivalReward(duration: Int, maxDuration: Int): Double =
      val t = duration.toDouble.max(1.0) // ticks so far, >=1
      val K = 0.1 // scale for total survival benefit
      val norm = math.log(1.0 + maxDuration)
      val totalSoFar = K * math.log(1.0 + t) / norm
      val totalPrev = K * math.log(1.0 + (t - 1.0)) / norm
      val rSurvStep = totalSoFar - totalPrev // small positive number
      rSurvStep

    // private def movementReward(action: Action[?]): Double =
    //   action match
    //     case MovementAction(1.0, 1.0) => 5.0
    //     case _ => -10.0

    private def distanceFromObstacle(env: Environment, entity: Agent): Double =
      entity.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

  end ObstacleAvoidance
end ObstacleAvoidanceRewardModule
