package io.github.srs.config.yaml.serializer.encoders

import io.circe.{ Encoder, Json }
import io.github.srs.model.Simulation
import io.circe.syntax.*

object Simulation:

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
