package io.github.srs.model.entity.dynamicentity.sensor

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.environment.Environment

/**
 * Represents a suite of sensors available for a dynamic entity.
 */
trait SensorSuite:
  /**
   * A sequence of proximity sensors that can sense the environment.
   *
   * Each sensor in this sequence is capable of detecting objects within its range.
   */
  val proximitySensors: Vector[ProximitySensor]

  /**
   * Senses the environment using the entity's sensors.
   *
   * @param entity
   *   the dynamic entity that is sensing the environment.
   * @param env
   *   the environment in which the entity is operating.
   * @return
   *   a collection of sensor readings.
   */
  def sense(entity: DynamicEntity, env: Environment): SensorReadings =
    SensorReadings(
      proximity = proximitySensors.map(s => SensorReading(s, s.sense(entity)(env))),
    )
end SensorSuite

object SensorSuite:

  /**
   * Creates a new `SensorSuite` with the specified proximity sensors.
   *
   * @param proximitySensors
   *   a variable number of `ProximitySensor` instances to include in the suite.
   * @return
   *   a new instance of `SensorSuite` containing the provided proximity sensors.
   */
  def apply(
      proximitySensors: ProximitySensor*,
  ): SensorSuite =
    new SensorSuiteImpl(proximitySensors.toVector)

  def empty: SensorSuite =
    SensorSuite()

  private class SensorSuiteImpl(
      override val proximitySensors: Vector[ProximitySensor],
  ) extends SensorSuite
end SensorSuite
