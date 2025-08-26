package io.github.srs

final case class AppArgs(
    headless: Boolean = false,
    path: Option[String] = None,
    simulationTime: Option[Long] = None,
    seed: Option[Long] = None,
)
