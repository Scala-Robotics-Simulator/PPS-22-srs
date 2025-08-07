package io.github.srs.config

import cats.effect.Sync
import cats.syntax.all.*
import fs2.io.file.{ Files, Path }
import fs2.text

/**
 * A trait for loading simulation configurations.
 * @tparam F
 *   the effect type
 */
trait ConfigLoader[F[_]]:
  /**
   * Loads the simulation configuration.
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  def load: F[ConfigResult[SimulationConfig]]

/**
 * A configuration loader that reads a YAML file from the specified path.
 * @param path
 *   the path to the YAML configuration file
 * @tparam F
 *   the effect type
 */
final case class YamlConfigLoader[F[_]: {Files, Sync}](path: Path) extends ConfigLoader[F]:

  /**
   * Loads the simulation configuration from the YAML file.
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  override def load: F[ConfigResult[SimulationConfig]] =
    for
      content <- Files[F].readAll(path).through(text.utf8.decode).compile.string
      config <- YamlParser.parse[F](content)
    yield config
