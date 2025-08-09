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

///**
// * A configuration for a simulation, containing the simulation and its environment.
// */
//trait SimulationConfig:
//  /**
//   * The simulation instance.
//   */
//  def simulation: Simulation
//
//  /**
//   * The environment in which the simulation runs.
//   */
//  def environment: Environment
//
//object SimulationConfig:
//
//  /**
//   * Creates a new `SimulationConfig` instance with the given simulation and environment.
//   *
//   * @param sim
//   *   the simulation instance
//   * @param env
//   *   the environment in which the simulation runs
//   * @return
//   *   a new `SimulationConfig` instance
//   */
//  def apply(sim: Simulation, env: Environment): SimulationConfig =
//    new SimulationConfig:
//      override def simulation: Simulation = sim
//      override def environment: Environment = env
