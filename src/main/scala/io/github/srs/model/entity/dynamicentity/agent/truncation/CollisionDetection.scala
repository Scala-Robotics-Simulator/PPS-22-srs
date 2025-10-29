package io.github.srs.model.entity.dynamicentity.agent.truncation

import scala.math.min

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.CollisionAvoidance.CollisionTriggerDistance
import cats.Id

/**
 * Generic truncation model for collision detection.
 *
 * Episode truncates (FAILURE) when the agent collides with an obstacle (proximity sensor < threshold).
 * This is reusable for any task where collision indicates failure:
 * phototaxis, obstacle avoidance, navigation, safe exploration, etc.
 */
final case class CollisionDetection() extends TruncationModel[Agent]:

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    val currentMin =
      entity.senseAll[Id](current.environment).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))
    currentMin < CollisionTriggerDistance