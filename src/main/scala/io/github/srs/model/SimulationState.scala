package io.github.srs.model

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.RNG

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
 * Compansion methods for the [[ModelModule.State]] to provide pretty-printing functionality.
 */
object SimulationState:

  extension (state: ModelModule.State)

    /**
     * Pretty prints the simulation state in a human-readable format.
     * @return
     *   a string representation of the simulation state.
     */
    def prettyPrint: String =
      s"""
         |--- SimulationState ---
         | Simulation Time : ${state.simulationTime.map(t => s"${t.toMillis} ms").getOrElse("∞")}
         | Elapsed Time    : ${state.elapsedTime.toMillis} ms
         | Δt              : ${state.dt.toMillis} ms
         | Speed           : ${state.simulationSpeed}
         | RNG Seed        : ${state.simulationRNG}
         | Status          : ${state.simulationStatus}
         | Environment     : ${state.environment}
         |-----------------------
        """.stripMargin
end SimulationState
