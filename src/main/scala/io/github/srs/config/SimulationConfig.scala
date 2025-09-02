package io.github.srs.config

import cats.effect.IO
import io.github.srs.CLILauncher.runMVC
import io.github.srs.mkInitialState
import io.github.srs.model.{ Simulation, SimulationState }
import io.github.srs.model.environment.EnvironmentParameters
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment

/**
 * A configuration for a simulation, containing the simulation and its environment.
 *
 * @tparam E
 *   the type of environment parameters
 * @param simulation
 *   the simulation instance
 * @param environment
 *   the environment in which the simulation runs
 */
final case class SimulationConfig[E <: EnvironmentParameters](
    simulation: Simulation,
    environment: E,
)

extension (simulationConfig: SimulationConfig[ValidEnvironment])

  /**
   * Runs the simulation in headless mode if a duration is specified.
   *
   * @return
   *   an IO effect that yields an Option containing the final SimulationState if a duration is specified or None if the
   *   simulation runs indefinitely.
   */
  infix def run: IO[Option[SimulationState]] =
    val initialState = mkInitialState(simulationConfig, headless = true)
    simulationConfig.simulation.duration match
      case None => IO.pure(None)
      case _ => runMVC(initialState).map(Some(_))
