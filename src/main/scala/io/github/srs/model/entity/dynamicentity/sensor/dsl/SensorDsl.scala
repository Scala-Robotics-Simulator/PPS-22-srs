package io.github.srs.model.entity.dynamicentity.sensor.dsl

import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor }
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.validation.DomainError

object SensorDsl:

  /**
   * Validates a Sensor to ensure it meets the domain constraints.
   *
   * @param sensor
   *   the Sensor to validate.
   * @return
   *   [[Right]] if the sensor is valid, or [[Left]] with a validation error.
   */
  def validateSensor(sensor: Sensor[Robot, Environment]): Validation[Sensor[Robot, Environment]] =
    import ProximitySensorDsl.validate
    sensor match
      case p: ProximitySensor[DynamicEntity, Environment] @unchecked => p.validate
      case s => Right[DomainError, Sensor[Robot, Environment]](s)
