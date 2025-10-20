package io.github.srs.model.entity.dynamicentity.sensor.dsl

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.sensor.ProximitySensor
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation
import io.github.srs.utils.types.PositiveDouble

object ProximitySensorDsl:

  def validateProximitySensor(
      sensor: ProximitySensor[DynamicEntity, Environment],
  ): Validation[ProximitySensor[DynamicEntity, Environment]] =
    sensor.validate

  /** Creates a new ProximitySensor with default properties. */
  def proximitySensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor()

  /** Generic extension methods for ProximitySensor with any entity type. */
  extension [E <: DynamicEntity, Env <: Environment](sensor: ProximitySensor[E, Env])

    /**
     * Sets the range of the proximity sensor.
     * @param range
     *   the range of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated range.
     */
    infix def withRange(range: Double): ProximitySensor[E, Env] =
      sensor.copy(range = range)

    /**
     * Sets the offset orientation of the sensor relative to the entity's orientation.
     * @param offset
     *   the orientation offset of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated offset.
     */
    infix def withOffset(offset: Orientation): ProximitySensor[E, Env] =
      sensor.copy(offset = offset)

    /**
     * Validates the properties of a sensor.
     */
    def validate: Validation[ProximitySensor[E, Env]] =
      for _ <- PositiveDouble(sensor.range).validate
      yield sensor
  end extension

end ProximitySensorDsl
