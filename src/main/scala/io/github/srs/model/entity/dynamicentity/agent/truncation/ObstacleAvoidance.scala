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
 * Default placeholder truncation model. Returns false for every transition — suitable as a neutral fallback.
 */
final case class ObstacleAvoidance() extends TruncationModel[Agent]:

  override def evaluate(prev: BaseState, current: BaseState, entity: Agent, action: Action[?]): Boolean =
    val currentMin =
      entity.senseAll[Id](current.environment).proximityReadings.foldLeft(1.0)((acc, sr) => min(acc, sr.value))
    currentMin < CollisionTriggerDistance
