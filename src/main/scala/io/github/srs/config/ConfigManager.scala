package io.github.srs.config

import java.nio.charset.StandardCharsets
import java.nio.file.Files as JNIOFiles

import cats.effect.Sync
import cats.syntax.all.*
import fs2.io.file.{ Files, Path }
import fs2.text
import io.github.srs.config.yaml.YamlManager
import cats.effect.kernel.Resource

/**
 * A trait for managing simulation configurations.
 *
 * @tparam F
 *   the effect type
 */
trait ConfigManager[F[_]]:
  /**
   * Loads the simulation configuration.
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  def load: F[ConfigResult[SimulationConfig]]

  /**
   * Saves the simulation configuration to a file.
   * @param config
   *   the configuration to save
   * @return
   *   an effect that completes when the configuration is saved
   */
  def save(config: SimulationConfig): F[Unit]

/**
 * A configuration manager that reads a YAML file from the specified path and provides methods to save a yaml
 * configuration.
 * @param path
 *   the path to the YAML configuration file
 * @tparam F
 *   the effect type
 */
final case class YamlConfigManager[F[_]: {Files, Sync}](path: Path) extends ConfigManager[F]:

  /**
   * Loads the simulation configuration from the YAML file.
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  override def load: F[ConfigResult[SimulationConfig]] =
    for
      content <- Files[F].readAll(path).through(text.utf8.decode).compile.string
      config <- YamlManager.parse[F](content)
    yield config

  /**
   * Saves the simulation configuration to a YAML file.
   *
   * @param config
   *   the configuration to save
   * @return
   *   an effect that completes when the configuration is saved
   */
  override def save(config: SimulationConfig): F[Unit] =
    val nioPath = path.toNioPath

    val writerResource = Resource.fromAutoCloseable(
      Sync[F].blocking(JNIOFiles.newBufferedWriter(nioPath, StandardCharsets.UTF_8)),
    )

    for
      yamlContent <- YamlManager.toYaml[F](config)
      _ <- writerResource.use { writer =>
        Sync[F].blocking(writer.write(yamlContent))
      }
    yield ()
end YamlConfigManager
