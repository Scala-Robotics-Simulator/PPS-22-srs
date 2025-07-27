package io.github.srs.lighting

import io.github.srs.model.Cell

/**
 * A trait that defines the behavior of light diffusion in an environment.
 *
 * This trait represents a pure functional capability that calculates how light spreads through an environment over
 * time. It takes a view of the world at time t and produces the next immutable light state.
 *
 * @tparam V
 *   The type of the environment view (contravariant)
 * @tparam S
 *   The type of the light state
 */
trait Diffuser[-V, S]:

  /**
   * Calculates the lighting state for the next time step.
   *
   * This is a curried function that takes:
   *   - A view of the current environment
   *
   *   - The current lighting state And produces the next lighting state.
   *
   * @param view
   *   The current view of the environment
   * @param currentState
   *   The current lighting state
   * @return
   *   The next lighting state
   */
  def step(view: V)(currentState: S): S

  /**
   * Retrieves the light intensity at a specific cell location.
   *
   * This is a convenience method for looking up the light level at a specific position in the current lighting state.
   *
   * @param currentState
   *   The current lighting state
   * @param cell
   *   The cell position to query
   * @return
   *   The light intensity at the specified cell position
   */
  def intensityAt(currentState: S)(cell: Cell): Double
end Diffuser
