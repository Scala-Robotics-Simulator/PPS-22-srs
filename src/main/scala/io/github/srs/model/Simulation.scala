package io.github.srs.model

import io.github.srs.utils.SimulationDefaults

/**
 * Simulation configuration. It contains the information needed to start a simulation.
 * @param duration
 *   The duration of the simulation in ticks. If None, the simulation will run indefinitely.
 * @param seed
 *   The seed for the random number generator. If None, a random seed will be used.
 */
final case class Simulation(
    duration: Option[Int] = SimulationDefaults.duration,
    seed: Option[Long] = SimulationDefaults.seed,
)

/**
 * Simulation companion object. It provides a dsl for creating and configuring a simulation.
 */
object Simulation:
  /**
   * Creates a new simulation instance with default values.
   * @return
   *   A new simulation instance with default values for duration and seed.
   */
  def simulation = Simulation()

  extension (simulation: Simulation)

    /**
     * Sets the duration of the simulation in ticks.
     * @param duration
     *   The duration of the simulation in ticks.
     * @return
     *   A new simulation instance with the specified duration.
     */
    infix def withDuration(duration: Int): Simulation =
      simulation.copy(duration = Some(duration))

    /**
     * Sets the seed for the random number generator.
     * @param seed
     *   The seed for the random number generator.
     * @return
     *   A new simulation instance with the specified seed.
     */
    infix def withSeed(seed: Long): Simulation =
      simulation.copy(seed = Some(seed))
  end extension
end Simulation
