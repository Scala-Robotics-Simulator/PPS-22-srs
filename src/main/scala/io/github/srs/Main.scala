package io.github.srs

import io.github.srs.view.{ ConfigurationCLI, ConfigurationGUI, ConfigurationView }
import cats.effect.unsafe.implicits.global

@main def main(args: String*): Unit =

  val headless = args.contains("--headless")
  val launcher = if headless then CLILauncher else GUILauncher
  val configurationView: ConfigurationView = if headless then ConfigurationCLI() else ConfigurationGUI()

  val runner = for
    cfg <- configurationView.init()
    state = mkInitialState(cfg)
    _ <- configurationView.close()
    _ <- launcher.runMVC(state)
  yield ()
  runner.unsafeRunSync()
