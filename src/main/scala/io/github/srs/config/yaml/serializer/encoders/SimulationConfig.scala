package io.github.srs.config.yaml.serializer.encoders

import io.circe
import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.SimulationConfig
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.utils.SimulationDefaults.Fields.Environment as EnvironmentFields
import io.github.srs.utils.SimulationDefaults.Fields.Simulation as SimulationFields
import io.github.srs.model.environment.Environment

/**
 * Encoders for SimulationConfig types.
 */
object SimulationConfig:

  /**
   * Encoder for SimulationConfig.
   * @return
   *   An Encoder that serializes SimulationConfig instances to JSON.
   */
  given Encoder[SimulationConfig[Environment]] =
    (config: SimulationConfig[Environment]) =>
      val baseFields = List(
        EnvironmentFields.Self -> config.environment.asJson,
      )

      val simulationFields =
        if config.simulation.duration.isDefined || config.simulation.seed.isDefined then
          List(SimulationFields.Self -> config.simulation.asJson)
        else List.empty[(String, Json)]

      Json.obj(simulationFields ++ baseFields*)

export SimulationConfig.given
