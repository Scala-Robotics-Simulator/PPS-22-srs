package io.github.srs.config.yaml.serializer

import io.circe.*
import io.circe.syntax.*
import io.circe.yaml.syntax.*
import io.github.srs.config.SimulationConfig
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.environment.Environment

object YamlSimulationConfigSerializer:

  /**
   * Serializes a `SimulationConfig` to a YAML string.
   *
   * @param config
   *   The `SimulationConfig` to serialize.
   * @return
   *   A YAML string representation of the `SimulationConfig`.
   */
  def serializeSimulationConfig(config: SimulationConfig[Environment]): String =
    config.asJson.asYaml.spaces2.replaceAll("\'", "") // Remove single quotes for YAML parser compatibility
