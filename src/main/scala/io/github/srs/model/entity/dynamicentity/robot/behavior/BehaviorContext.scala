package io.github.srs.model.entity.dynamicentity.robot.behavior

import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.utils.random.RNG

/**
 * Input context for behavior evaluation. Designed to be extended as new behaviors need more information.
 */
final case class BehaviorContext(
    sensorReadings: SensorReadings,
    rng: RNG,
)
