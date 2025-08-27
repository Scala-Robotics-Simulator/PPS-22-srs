package io.github.srs

/**
 * Arguments for the application.
 *
 * @param headless
 *   true if the application should run in headless mode (CLI), false for GUI mode.
 * @param path
 *   the path to the configuration file.
 * @param simulationTime
 *   the time to run the simulation for (in milliseconds).
 * @param seed
 *   the seed for the random number generator.
 */
final case class AppArgs(
    headless: Boolean = false,
    path: Option[String] = None,
    simulationTime: Option[Long] = None,
    seed: Option[Long] = None,
)
