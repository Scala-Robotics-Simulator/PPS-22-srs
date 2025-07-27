package io.github.srs.model.lighting

import io.github.srs.model.Cell

/**
 * Diffuser trait for calculating light diffusion in an environment.
 *
 * Represents a pure functional capability that calculates how light spreads through an environment over
 * time. It takes a view of the world at time t and produces the next immutable light state.
 *
 * @tparam V
 *   The type of the environment view
 * @tparam S
 *   The type of the light state
 */
trait Diffuser[-V, S]:

  /**
   * Calculates the lighting state for the next time step.
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
