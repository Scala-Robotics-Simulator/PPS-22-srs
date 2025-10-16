package io.github.srs

import io.github.srs.view.{ ConfigurationCLI, ConfigurationGUI, ConfigurationView }
import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.Logger

private val logger = Logger("SRS_main")

@main def main(args: String*): Unit =
  ArgParser.parse(args) match
    case Some(parsed) =>
      if parsed.reinforcementLearning then Runners.runRL()
      else Runners.runClassic(parsed)
    case None =>
      logger.error("Failed to parse arguments.")

object Runners:

  def runClassic(parsed: AppArgs): Unit =
    val configurationView: ConfigurationView =
      if parsed.headless
      then ConfigurationCLI(parsed.path, parsed.simulationTime, parsed.seed)
      else ConfigurationGUI()
    val run = Launcher.run(parsed.headless)

    val runner = for
      cfg <- configurationView.init()
      _ <- configurationView.close()
      _ <- run(cfg)
    yield ()

    runner.unsafeRunSync()

  def runRL(): Unit =
    RLLauncher.run.unsafeRunSync()
