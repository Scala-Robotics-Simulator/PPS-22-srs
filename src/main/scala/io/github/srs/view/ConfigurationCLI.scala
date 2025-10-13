package io.github.srs.view

import cats.effect.IO
import io.github.srs.config.{ ConfigError, SimulationConfig, YamlConfigManager }
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.validate
import com.typesafe.scalalogging.Logger

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

    private val logger = Logger(getClass.getName)

    /**
     * @inheritdoc
     */
    override def init(): IO[SimulationConfig[ValidEnvironment]] =
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

        cfgWithParams: SimulationConfig[ValidEnvironment] = cfg.copy(
          simulation = cfg.simulation.copy(
            duration = Some(simTime),
            seed = Some(seed),
          ),
        )
      yield cfgWithParams

    /**
     * Asks the user for the simulation time in milliseconds.
     * @return
     *   an [[IO]] action that yields the simulation time as a [[Long]].
     */
    private def askSimulationTime: IO[Long] =
      for
        _ <- IO.print("Enter simulation time (ms): ")
        line <- IO.readLine
        duration <- IO(line.toLong)
          .handleErrorWith(_ => IO(logger.error("Invalid input, try again.")) *> askSimulationTime)
      yield duration

    /**
     * Asks the user for a random seed.
     * @return
     *   an [[IO]] action that yields the random seed as a [[Long]].
     */
    private def askSeed: IO[Long] =
      for
        _ <- IO.print("Enter random seed: ")
        line <- IO.readLine
        seed <- IO(line.toLong)
          .handleErrorWith(_ => IO(logger.error("Invalid input, try again.")) *> askSeed)
      yield seed

    /**
     * Loads the configuration from the specified path.
     * @param path
     *   the path to the configuration file.
     * @return
     *   an [[IO]] action that yields the loaded [[SimulationConfig]].
     */
    private def loadConfig(path: String): IO[SimulationConfig[ValidEnvironment]] =
      val configPath = fs2.io.file.Path.fromNioPath(java.nio.file.Paths.get(path))
      YamlConfigManager[IO](configPath).load.attempt.flatMap:
        case Left(_: java.nio.file.NoSuchFileException) =>
          IO.raiseError[SimulationConfig[ValidEnvironment]](new RuntimeException(s"File not found: $path"))
        case Left(ex) =>
          IO.raiseError[SimulationConfig[ValidEnvironment]](
            new RuntimeException(s"Failed to load file: ${ex.getMessage}"),
          )
        case Right(Left(errors: Seq[ConfigError] @unchecked)) =>
          IO.raiseError[SimulationConfig[ValidEnvironment]](
            new RuntimeException(s"Parsing failed:\n${errors.mkString("\n")}"),
          )
        case Right(Right(cfg)) =>
          cfg.environment.validate match
            case Left(error) =>
              IO.raiseError[SimulationConfig[ValidEnvironment]](
                new RuntimeException(s"Environment validation error: ${error.errorMessage}"),
              )
            case Right(validEnv) =>
              IO.pure(SimulationConfig[ValidEnvironment](cfg.simulation, validEnv))

    end loadConfig

    /**
     * @inheritdoc
     */
    override def close(): IO[Unit] =
      IO.println("Closing CLI Configuration View")

  end ConfigurationCLIImpl
end ConfigurationCLI
