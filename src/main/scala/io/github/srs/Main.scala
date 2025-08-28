package io.github.srs

import io.github.srs.view.{ ConfigurationCLI, ConfigurationGUI, ConfigurationView }
import cats.effect.unsafe.implicits.global

@main def main(args: String*): Unit =
  ArgParser.parse(args) match
    case Some(parsed) =>
      val launcher = if parsed.headless then CLILauncher else GUILauncher
      val configurationView: ConfigurationView =
        if parsed.headless then ConfigurationCLI(parsed.path, parsed.simulationTime, parsed.seed)
        else ConfigurationGUI()

      val runner = for
        cfg <- configurationView.init()
        state = mkInitialState(cfg, parsed.headless)
        _ <- configurationView.close()
        _ <- launcher.runMVC(state)
      yield ()

      runner.unsafeRunSync()

    case None =>
      println("Failed to parse arguments.")
