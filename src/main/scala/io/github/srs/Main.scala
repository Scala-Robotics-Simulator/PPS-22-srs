package io.github.srs

import io.github.srs.view.{ ConfigurationCLI, ConfigurationGUI, ConfigurationView }
import cats.effect.unsafe.implicits.global

@main def main(args: String*): Unit =
  ArgParser.parse(args) match
    case Some(parsed) =>
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

    case None =>
      println("Failed to parse arguments.")
