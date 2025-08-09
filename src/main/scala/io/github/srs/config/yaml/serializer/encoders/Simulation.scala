package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.Simulation

/**
 * Encoders for Simulation types.
 */
object Simulation:

  /**
   * Encoder for Simulation.
   * @return
   *   An Encoder that serializes Simulation instances to JSON.
   */
  given Encoder[Simulation] = (simulation: Simulation) =>
    Json
      .obj()
      .deepMerge(
        simulation.duration.map("duration" -> _.asJson).toList.toMap.asJson,
      )
      .deepMerge(
        simulation.seed.map("seed" -> _.asJson).toList.toMap.asJson,
      )

export Simulation.given
