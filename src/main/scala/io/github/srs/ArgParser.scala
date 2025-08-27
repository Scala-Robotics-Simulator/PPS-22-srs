package io.github.srs

import scopt.OParser

/**
 * Command-line argument parser for the Scala Robotics Simulator application.
 */
object ArgParser:

  private val manifest = getClass.getPackage
  private val appVersion = Option(manifest.getImplementationVersion).getOrElse("dev")
  private val appTitle = Option(manifest.getImplementationTitle).getOrElse("SRS")

  /**
   * The command-line arguments builder.
   */
  private val builder = OParser.builder[AppArgs]

  /**
   * The command-line argument parser.
   */
  private val parser =
    import builder.*
    OParser.sequence(
      programName("Scala Robotics Simulator"),
      head(appTitle, appVersion),
      opt[Unit]("headless")
        .action((_, c) => c.copy(headless = true))
        .text("Run in CLI headless mode (no GUI)"),
      opt[String]("path")
        .action((path, c) => c.copy(path = Some(path)))
        .valueName("<path>")
        .text("Path to the YAML configuration file"),
      opt[Long]("duration")
        .action((duration, c) => c.copy(simulationTime = Some(duration)))
        .valueName("<milliseconds>")
        .text("Total simulation time in milliseconds"),
      opt[Long]("seed")
        .action((seed, c) => c.copy(seed = Some(seed)))
        .valueName("<seed>")
        .text("Random seed for simulation reproducibility"),
      help("help").text("Print this help message"),
      version("version").text("Show version"),
    )
  end parser

  /**
   * Parses the command-line arguments.
   *
   * @param args
   *   the command-line arguments.
   * @return
   *   Some(AppArgs) if parsing was successful, None otherwise.
   */
  def parse(args: Seq[String]): Option[AppArgs] =
    OParser.parse(parser, args, AppArgs())
end ArgParser
