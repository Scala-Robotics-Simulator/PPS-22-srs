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
import io.github.srs.model.entity.dynamicentity.action.MovementAction
import io.github.srs.model.ModelModule.BaseState

object ObstacleAvoidanceRewardModule:

  /**
   * Reward model focused on obstacle avoidance.
   */
  final case class DQObstacleAvoidance() extends RewardModel[Agent]:
    private val logger = Logger(getClass.getName)

    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)

    override def evaluate(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
    ): Double =
      val currentMin = distanceFromObstacle(current.environment, entity)
      val rClear = clearanceReward(prev.environment, current.environment, entity, -2) * 10
      val rColl = if currentMin < CollisionTriggerDistance then -1000.0 else 0.0
      val prevAgent = getAgentFromId(entity, prev)
      val rMove = moveReward(entity, prevAgent, action, currentMin)

      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      logger.info(s"movement penalty: $rMove")

      val reward = rClear + rColl + rMove
      logger.info(s"total reward: $reward")

      reward

    end evaluate

  end DQObstacleAvoidance

  final case class QObstacleAvoidance() extends RewardModel[Agent]:
    private val logger = Logger(getClass.getName)

    private def getAgentFromId(agent: Agent, state: BaseState): Agent =
      state.environment.entities.collectFirst { case a: Agent if a.id.equals(agent.id) => a }.getOrElse(agent)

    override def evaluate(
        prev: BaseState,
        current: BaseState,
        entity: Agent,
        action: Action[?],
    ): Double =
      val currentMin = distanceFromObstacle(current.environment, entity)
      val rClear = clearanceReward(prev.environment, current.environment, entity, -5)
      val rColl = if currentMin < CollisionTriggerDistance then -1000.0 else 0.0
      val prevAgent = getAgentFromId(entity, prev)
      val rMove = moveReward(entity, prevAgent, action, currentMin)

      logger.info(s"clearance reward: $rClear")
      logger.info(s"collision penalty: $rColl")
      logger.info(s"movement penalty: $rMove")

      val reward = rClear + rColl + rMove
      logger.info(s"total reward: $reward")

      reward

    end evaluate

  end QObstacleAvoidance

  private def moveReward(agent: Agent, prevAgent: Agent, action: Action[?], currentMin: Double): Double =
    if agent.position == prevAgent.position then if currentMin < 0.1 then 0.5 else -1.0
    else
      action match
        case ma: MovementAction[?] if ma.leftSpeed == 0.0 || ma.rightSpeed == 0.0 =>
          if currentMin > 0.5 then -1.0 else 1.0
        case ma: MovementAction[?] if ma.leftSpeed > 0 && ma.rightSpeed > 0 => 2.0
        case _ => -1.0

  private def clearanceReward(prev: Environment, current: Environment, entity: Agent, base: Double): Double =
    val prevMin = distanceFromObstacle(prev, entity)
    val currMin = distanceFromObstacle(current, entity)
    val delta = currMin - prevMin
    val rChange = delta * 10.0
    val rProximity = -exp(base * currMin)
    (rChange + rProximity) / 2

  private def distanceFromObstacle(env: Environment, entity: Agent): Double =
    val agent =
      env.entities.collectFirst { case a: Agent if a.id.toString == entity.id.toString => a }.getOrElse(entity)
    agent.senseAll[Id](env).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))

end ObstacleAvoidanceRewardModule
