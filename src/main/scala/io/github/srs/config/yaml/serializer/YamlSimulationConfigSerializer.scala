package io.github.srs.config.yaml.serializer

import io.github.srs.config.SimulationConfig
import io.circe.yaml.syntax.*
import io.circe.*
import io.circe.syntax.*
import io.github.srs.config.yaml.serializer.encoders.given

object YamlSimulationConfigSerializer:

  def serializeSimulationConfig(config: SimulationConfig): String =
    config.asJson.asYaml.spaces2
