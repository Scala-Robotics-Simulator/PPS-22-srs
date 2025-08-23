package io.github.srs.model.entity.dynamicentity.sensor.dsl

import io.github.srs.model.ModelModule
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.sensor.ProximitySensor

object ProximitySensorDsl:

  /** Creates a new ProximitySensor with default properties. */
  def proximitySensor: ProximitySensor[DynamicEntity, ModelModule.State] = ProximitySensor()

  /** Extension methods for ProximitySensor to allow DSL-like configuration. */
  extension (sensor: ProximitySensor[DynamicEntity, ModelModule.State])

    /**
     * Sets the range of the proximity sensor.
     * @param range
     *   the range of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated range.
     */
    infix def withRange(range: Double): ProximitySensor[DynamicEntity, ModelModule.State] =
      sensor.copy(range = range)

    /**
     * Sets the offset orientation of the sensor relative to the entity's orientation.
     * @param offset
     *   the orientation offset of the sensor.
     * @return
     *   a new [[ProximitySensor]] instance with the updated offset.
     */
    infix def withOffset(offset: Orientation): ProximitySensor[DynamicEntity, ModelModule.State] =
      sensor.copy(offset = offset)
  end extension
end ProximitySensorDsl
