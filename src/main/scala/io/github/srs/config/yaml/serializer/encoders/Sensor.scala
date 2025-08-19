package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor }

/**
 * Encoders for Sensor types.
 */
object Sensor:

  /**
   * Encoder for Sensor types.
   * @return
   *   An Encoder that serializes Sensor instances to JSON.
   */
  given Encoder[Sensor[?, ?]] = { case ps: ProximitySensor[?, ?] =>
    Json.obj("proximitySensor" -> Encoder[ProximitySensor[?, ?]].apply(ps))
  }

  /**
   * Encoder for ProximitySensor.
   * @return
   *   An Encoder that serializes ProximitySensor instances to JSON.
   */
  given Encoder[ProximitySensor[?, ?]] = (sensor: ProximitySensor[?, ?]) =>
    Json.obj(
      "offset" -> sensor.offset.degrees.asJson,
      "range" -> sensor.range.asJson,
    )
end Sensor

export Sensor.given
