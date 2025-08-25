package io.github.srs.config.yaml

import cats.effect.Sync
import io.github.srs.config.yaml.parser.YamlSimulationConfigParser
import io.github.srs.config.yaml.serializer.YamlSimulationConfigSerializer
import io.github.srs.config.{ ConfigResult, SimulationConfig }
import io.github.srs.model.environment.Environment

/**
 * A parser for YAML configuration files.
 */
object YamlManager:
  /**
   * Parses a YAML configuration string into a `SimulationConfig`.
   * @param content
   *   the YAML configuration content as a string
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   * @example
   *   The following example shows the structure of a YAML configuration:
   *   {{{
   *     simulation:
   *       duration: 1000
   *       seed: 42
   *
   *     environment:
   *       width: 10
   *       height: 10
   *       entities:
   *         - obstacle:
   *             position: [5, 5]
   *             orientation: 0.0
   *             width: 1.0
   *             height: 1.0
   *         - light:
   *             position: [2, 2]
   *             illuminationRadius: 5.0
   *             intensity: 1.0
   *             attenuation: 1.0
   *         - robot:
   *             position: [3, 1]
   *             orientation: 90.0
   *             radius: 0.5
   *             speed: 1.0
   *             withProximitySensors: true
   *             withLightSensors: false
   *   }}}
   */

  def parse[F[_]: Sync](content: String): F[ConfigResult[SimulationConfig[Environment]]] =
    Sync[F].pure(YamlSimulationConfigParser.parseSimulationConfig(content))

  /**
   * Converts a `SimulationConfig` to a YAML string.
   * @param config
   *   the `SimulationConfig` to convert
   * @tparam F
   *   the effect type
   * @return
   *   a `F[String]` containing the YAML representation of the configuration
   */
  def toYaml[F[_]: Sync](config: SimulationConfig[Environment]): F[String] =
    Sync[F].pure(YamlSimulationConfigSerializer.serializeSimulationConfig(config))
end YamlManager
