package io.github.srs.config.yaml.serializer.encoders

import io.circe
import io.circe.syntax.*
import io.circe.{Encoder, Json}
import io.github.srs.config.SimulationConfig
import io.github.srs.config.yaml.serializer.encoders.given

object SimulationConfig:

  given Encoder[SimulationConfig] = (config: SimulationConfig) =>
    val baseFields = List(
      "environment" -> config.environment.asJson,
    )

    val simulationFields =
      if config.simulation.duration.isDefined || config.simulation.seed.isDefined then
        List("simulation" -> config.simulation.asJson)
      else List.empty[(String, Json)]

    Json.obj(simulationFields ++ baseFields*)

export SimulationConfig.given
