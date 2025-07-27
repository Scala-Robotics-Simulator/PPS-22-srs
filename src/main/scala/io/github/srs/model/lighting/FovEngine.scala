package io.github.srs.model.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.Cell

/**
 * Represents a Field of View (FOV) engine for calculating light intensities in a grid-based environment.
 *
 * The [[FovEngine]] is used to calculate the light intensities emanating from a specific origin cell within a defined
 * radius, considering factors such as obstacles and resistance values
 */
trait FovEngine:

  /**
   * Computes the field of view (FOV) for a given resistance map, origin cell, and radius.
   *
   * @param resistance
   *   A 2D array representing the resistance values at each cell in the grid. Higher values indicate more resistance to
   *   light.
   * @param origin
   *   The starting cell from which the FOV is computed.
   * @param radius
   *   The maximum distance from the origin to consider for visibility.
   * @return
   *   An immutable sequence of doubles representing the light intensity at each cell in the grid.
   */
  def compute(resistance: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double]