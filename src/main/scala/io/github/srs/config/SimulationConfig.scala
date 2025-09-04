package io.github.srs.config

import io.github.srs.model.Simulation
import io.github.srs.model.environment.EnvironmentParameters

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
