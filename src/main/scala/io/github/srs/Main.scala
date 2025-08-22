package io.github.srs

import io.github.srs.view.ConfigurationView
import cats.effect.unsafe.implicits.global

@main def main(): Unit =
  val configurationView = ConfigurationView()
  val runner = for
    cfg <- configurationView.init()
    state = mkInitialState(cfg)
    _ <- Launcher.runMVC(state)
  yield ()
  runner.unsafeRunSync()
