package io.github.srs.model.entity.dynamicentity

import cats.effect.IO
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReadings }
import io.github.srs.model.environment.Environment

/**
 * Represents a dynamic entity. A dynamic entity is an entity that can change its state over time.
 */
trait DynamicEntity extends Entity:
  /**
   * Returns the sequence of actuators that can act upon a dynamic entity.
   * @return
   *   the sequence of actuators.
   */
  def actuators: Seq[Actuator[? <: DynamicEntity]]

  /**
   * Returns the sequence of sensors that can sense the environment for a dynamic entity.
   * @return
   *   the sequence of sensors.
   */
  def sensors: Vector[Sensor[? <: DynamicEntity, ? <: Environment]]

  /**
   * Returns the behavior of the dynamic entity, which defines how it reacts to sensor readings.
   * @return
   *   the behavior of the dynamic entity.
   */
  def behavior: Behavior[SensorReadings, Action[IO]]
end DynamicEntity
