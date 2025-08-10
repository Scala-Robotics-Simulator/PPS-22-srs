package io.github.srs.config

import io.github.srs.model.Simulation
import io.github.srs.model.environment.Environment

/**
 * A configuration for a simulation, containing the simulation and its environment.
 *
 * @param simulation
 *   the simulation instance
 * @param environment
 *   the environment in which the simulation runs
 */
final case class SimulationConfig(
    simulation: Simulation,
    environment: Environment,
)
