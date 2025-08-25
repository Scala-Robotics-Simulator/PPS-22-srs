package io.github.srs.view

import java.nio.file.NoSuchFileException

import cats.effect.IO
import fs2.io.file.Path
import io.github.srs.config.{ ConfigError, SimulationConfig, YamlConfigManager }

/**
 * ConfigurationView companion object with factory method to create an instance of a CLI-based configuration view.
 */
object ConfigurationCLI:

  /**
   * Factory method to create an instance of ConfigurationView.
   *
   * @return
   *   a new instance of ConfigurationView.
   */
  def apply(): ConfigurationView = new ConfigurationCLIImpl

  private class ConfigurationCLIImpl extends ConfigurationView:

    override def init(): IO[SimulationConfig] =
      for
        _ <- IO.println("Welcome to the Scala Robotics Simulator CLI Configuration")
        _ <- IO.println("Please enter the configuration path:")
        path <- IO.readLine
        configPath = Path.fromNioPath(java.nio.file.Paths.get(path))
        cfg <- YamlConfigManager[IO](configPath).load.attempt.flatMap:
          case Left(_: NoSuchFileException) =>
            IO.raiseError[SimulationConfig](new RuntimeException(s"File not found: $path"))
          case Left(ex) =>
            IO.raiseError[SimulationConfig](new RuntimeException(s"Failed to load file: ${ex.getMessage}"))
          case Right(Left(errors: Seq[ConfigError] @unchecked)) =>
            IO.raiseError[SimulationConfig](
              new RuntimeException(
                s"Parsing failed with errors:\n${errors.mkString("\n")}",
              ),
            )
          case Right(Right(config)) => IO.pure(config)
      yield cfg

    override def close(): IO[Unit] =
      IO.println("Closing CLI Configuration View")
  end ConfigurationCLIImpl
end ConfigurationCLI
