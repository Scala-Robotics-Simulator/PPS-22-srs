package io.github.srs.model.logic

import cats.effect.IO
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.RNG

/**
 * Logic for random number generation and state updates.
 * @tparam S
 *   the type of the model state.
 */
trait RandomLogic[S <: ModelModule.BaseState]:
  /**
   * Generate a new random state based on the current state and RNG.
   * @param s
   *   the current state.
   * @param rng
   *   the random number generator.
   * @return
   *   an [[cats.effect.IO]] effect producing the new state with updated RNG.
   */
  def random(s: S, rng: RNG): IO[S]

/**
 * Companion object for [[RandomLogic]] containing given instances.
 */
object RandomLogic:

  given RandomLogic[SimulationState] with

    def random(s: SimulationState, rng: RNG): IO[SimulationState] =
      IO.pure(s.copy(simulationRNG = rng))
