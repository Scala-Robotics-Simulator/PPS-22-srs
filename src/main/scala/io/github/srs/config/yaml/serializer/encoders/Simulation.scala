package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.Simulation
import io.github.srs.utils.SimulationDefaults.Fields.Simulation as SimulationFields

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
        simulation.duration.map(SimulationFields.duration -> _.asJson).toList.toMap.asJson,
      )
      .deepMerge(
        simulation.seed.map(SimulationFields.seed -> _.asJson).toList.toMap.asJson,
      )

export Simulation.given
