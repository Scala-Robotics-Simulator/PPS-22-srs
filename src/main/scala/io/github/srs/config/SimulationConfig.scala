package io.github.srs.config

import io.github.srs.CLILauncher.runMVC
import io.github.srs.mkInitialState
import io.github.srs.model.{ Simulation, SimulationState }
import io.github.srs.model.environment.EnvironmentParameters
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import cats.effect.unsafe.implicits.global

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
   * Runs the simulation in headless mode and returns the final state if the duration is defined.
   * @return
   *   the final state of the simulation wrapped in Some, or None if the duration is not defined.
   */
  infix def run: Option[SimulationState] =

    val initialState = mkInitialState(simulationConfig, headless = true)

    simulationConfig.simulation.duration match
      case None => None
      case _ => Some(runMVC(initialState).unsafeRunSync())
