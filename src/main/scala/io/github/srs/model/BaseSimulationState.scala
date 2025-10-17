package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.config.SimulationConfig
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
final case class BaseSimulationState(
    override val simulationTime: Option[FiniteDuration],
    override val elapsedTime: FiniteDuration = FiniteDuration(0, MILLISECONDS),
    override val dt: FiniteDuration = FiniteDuration(100, MILLISECONDS),
    override val simulationRNG: RNG,
    override val environment: ValidEnvironment,
) extends ModelModule.BaseState

/**
 * Companion methods for the [[ModelModule.State]] to provide pretty-printing functionality.
 */
object BaseSimulationState:

  /**
   * Creates the initial state of the simulation based on the provided configuration.
   *
   * @param cfg
   *   the simulation configuration to use for initializing the state
   * @return
   *   the initial state of the simulation
   */
  def from(cfg: SimulationConfig[ValidEnvironment]): BaseSimulationState =
    val rng = SimpleRNG(cfg.simulation.seed.getOrElse(42))
    from(cfg, rng)

  def from(cfg: SimulationConfig[ValidEnvironment], rng: RNG): BaseSimulationState =
    BaseSimulationState(
      simulationTime = cfg.simulation.duration.map(FiniteDuration(_, MILLISECONDS)),
      simulationRNG = rng,
      environment = cfg.environment,
    )
end BaseSimulationState
