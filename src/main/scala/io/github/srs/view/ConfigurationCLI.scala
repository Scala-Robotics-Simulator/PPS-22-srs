package io.github.srs.view

import cats.effect.IO
import io.github.srs.config.{ SimulationConfig, YamlConfigManager }

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
  def apply(
      pathOpt: Option[String],
      simulationTimeOpt: Option[Long],
      seedOpt: Option[Long],
  ): ConfigurationView =
    new ConfigurationCLIImpl(pathOpt, simulationTimeOpt, seedOpt)

  private class ConfigurationCLIImpl(
      pathOpt: Option[String],
      simulationTimeOpt: Option[Long],
      seedOpt: Option[Long],
  ) extends ConfigurationView:

    override def init(): IO[SimulationConfig] =
      for
        configPath <- pathOpt match
          case Some(p) => IO.pure(p)
          case None =>
            IO.println("Please enter the configuration path:") *> IO.readLine

        cfg <- loadConfig(configPath)

        simTime <- simulationTimeOpt match
          case Some(t) => IO.pure(t)
          case None =>
            cfg.simulation.duration match
              case Some(t) => IO.pure(t)
              case None => askSimulationTime

        seed <- seedOpt match
          case Some(s) => IO.pure(s)
          case None =>
            cfg.simulation.seed match
              case Some(s) => IO.pure(s)
              case None => askSeed

        cfgCLI = cfg.copy(
          simulation = cfg.simulation.copy(
            duration = Some(simTime),
            seed = Some(seed),
          ),
        )
      yield cfgCLI

    private def askSimulationTime: IO[Long] =
      for
        _ <- IO.print("Enter simulation duration (ms): ")
        line <- IO.readLine
        duration <- IO(line.toLong)
          .handleErrorWith(_ => IO.println("Invalid input, try again.") *> askSimulationTime)
      yield duration

    private def askSeed: IO[Long] =
      for
        _ <- IO.print("Enter random seed: ")
        line <- IO.readLine
        seed <- IO(line.toLong)
          .handleErrorWith(_ => IO.println("Invalid input, try again.") *> askSeed)
      yield seed

    private def loadConfig(path: String): IO[SimulationConfig] =
      val configPath = fs2.io.file.Path.fromNioPath(java.nio.file.Paths.get(path))
      YamlConfigManager[IO](configPath).load.attempt.flatMap:
        case Left(_: java.nio.file.NoSuchFileException) =>
          IO.raiseError[SimulationConfig](new RuntimeException(s"File not found: $path"))
        case Left(ex) =>
          IO.raiseError[SimulationConfig](new RuntimeException(s"Failed to load file: ${ex.getMessage}"))
        case Right(Left(errors: Seq[io.github.srs.config.ConfigError] @unchecked)) =>
          IO.raiseError[SimulationConfig](new RuntimeException(s"Parsing failed:\n${errors.mkString("\n")}"))
        case Right(Right(config)) => IO.pure(config)

    override def close(): IO[Unit] =
      IO.println("Closing CLI Configuration View")

  end ConfigurationCLIImpl
end ConfigurationCLI
