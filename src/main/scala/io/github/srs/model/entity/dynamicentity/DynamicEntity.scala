package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.sensor.SensorSuite

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
   * Returns the suite of sensors available for this dynamic entity.
   * @return
   *   the sensor suite.
   */
  val sensors: SensorSuite
