package io.github.srs.config

import cats.effect.Sync

/**
 * A parser for YAML configuration files.
 */
object YamlParser:
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

  def parse[F[_]: Sync](content: String): F[ConfigResult[SimulationConfig]] =
    Sync[F].pure(YamlSimulationConfigParser.parseSimulationConfig(content))
end YamlParser
