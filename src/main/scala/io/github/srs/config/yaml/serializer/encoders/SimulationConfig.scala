package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.SimulationConfig
import io.github.srs.config.yaml.serializer.encoders.given

object SimulationConfig:

  given Encoder[SimulationConfig] = (config: SimulationConfig) =>
    Json.obj(
      "simulation" -> config.simulation.asJson,
      "environment" -> config.environment.asJson,
    )

export SimulationConfig.given
