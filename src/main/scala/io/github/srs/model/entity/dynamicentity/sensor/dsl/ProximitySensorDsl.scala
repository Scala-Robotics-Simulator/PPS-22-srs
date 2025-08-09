package io.github.srs.model.entity.dynamicentity.sensor.dsl

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.sensor.ProximitySensor
import io.github.srs.model.environment.Environment

object ProximitySensorDsl:

  /** Creates a new ProximitySensor with default properties. */
  def proximitySensor: ProximitySensor[DynamicEntity, Environment] = ProximitySensor()

  /** Extension methods for ProximitySensor to allow DSL-like configuration. */
  extension (sensor: ProximitySensor[DynamicEntity, Environment])

    /**
     * Sets the range of the proximity sensor.
     * @param range
     *   the range of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated range.
     */
    infix def withRange(range: Double): ProximitySensor[DynamicEntity, Environment] =
      sensor.copy(range = range)

    /**
     * Sets the distance from the center of the entity to the sensor.
     * @param distance
     *   the distance from the entity's center to the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated distance.
     */
    infix def withDistance(distance: Double): ProximitySensor[DynamicEntity, Environment] =
      sensor.copy(distance = distance)

    /**
     * Sets the offset orientation of the sensor relative to the entity's orientation.
     * @param offset
     *   the orientation offset of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated offset.
     */
    infix def withOffset(offset: Orientation): ProximitySensor[DynamicEntity, Environment] =
      sensor.copy(offset = offset)
  end extension
end ProximitySensorDsl
