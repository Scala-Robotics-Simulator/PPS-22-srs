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
import io.github.srs.model.entity.dynamicentity.action.MovementAction

/**
 * Reward model focused on obstacle avoidance.
 */
final case class ObstacleAvoidance() extends RewardModel[Agent]:
  private val logger = Logger(getClass.getName)

  private var state: Int = 0

  override def evaluate(
      prev: Environment,
      current: Environment,
      entity: Agent,
      action: Action[?],
  ): Double =
    logger.debug(s"number of ticks: $state")
    val currentMin =
      entity.senseAll[Id](current).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))
    val prevAgent =
      prev.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)

    val prevMin =
      prevAgent.senseAll[Id](prev).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

    logger.debug(s"previous min proximity: $prevMin")
    logger.debug(s"current min proximity: $currentMin")

    val rSurv = state * 0.1
    logger.debug(s"survival reward: $rSurv")
    val rClear = pow(5, currentMin / 0.2)
    logger.debug(s"clearance reward: $rClear")
    val rColl = if currentMin < CollisionTriggerDistance then -100.0 else 0.0
    val rMove = action match
      case MovementAction(1.0, 1.0) => 10
      case MovementAction(-1.0, 1.0) => 3
      case MovementAction(1.0, -1.0) => 3
      case _ => 0

    val reward = rSurv + rClear + rColl + rMove

    state = state + 1
    reward
  end evaluate
end ObstacleAvoidance
