package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.config.SimulationConfig
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.{ RNG, SimpleRNG }

/**
 * Simulation state case class that holds the current state of the simulation.
 * @param simulationTime
 *   the current simulation time, if applicable.
 * @param elapsedTime
 *   the elapsed time since the start of the simulation.
 * @param dt
 *   the time step for each simulation update.
 * @param simulationSpeed
 *   the speed at which the simulation is running.
 * @param simulationRNG
 *   the random number generator used in the simulation.
 * @param simulationStatus
 *   the current status of the simulation.
 * @param environment
 *   the current environment of the simulation.
 */
final case class SimulationState(
    override val simulationTime: Option[FiniteDuration],
    override val elapsedTime: FiniteDuration = FiniteDuration(0, MILLISECONDS),
    override val dt: FiniteDuration = FiniteDuration(100, MILLISECONDS),
    override val simulationSpeed: SimulationSpeed,
    override val simulationRNG: RNG,
    override val simulationStatus: SimulationStatus,
    override val environment: ValidEnvironment,
) extends ModelModule.State

/**
 * Companion methods for the [[ModelModule.State]] to provide pretty-printing functionality.
 */
object SimulationState:

  /**
   * Creates the initial state of the simulation based on the provided configuration.
   *
   * @param cfg
   *   the simulation configuration to use for initializing the state
   * @return
   *   the initial state of the simulation
   */
  def from(cfg: SimulationConfig[ValidEnvironment], headless: Boolean): SimulationState =
    val speed = if headless then SimulationSpeed.SUPERFAST else SimulationSpeed.NORMAL
    val status = if headless then SimulationStatus.RUNNING else SimulationStatus.PAUSED
    val rng = SimpleRNG(cfg.simulation.seed.getOrElse(42))
    SimulationState(
      simulationTime = cfg.simulation.duration.map(FiniteDuration(_, MILLISECONDS)),
      simulationSpeed = speed,
      simulationRNG = rng,
      simulationStatus = status,
      environment = cfg.environment,
    )
end SimulationState
