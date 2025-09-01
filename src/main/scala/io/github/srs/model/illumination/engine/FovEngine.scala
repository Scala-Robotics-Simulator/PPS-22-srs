package io.github.srs.model.illumination.engine

import scala.collection.immutable.ArraySeq

import io.github.srs.model.illumination.model.Grid

/**
 * Represents a Field of View (FoV) engine for calculating light intensities in a grid-based environment.
 */
trait FovEngine:

  /**
   * Compute a field-of-view / light propagation field.
   *
   * @param OcclusionGrid
   *   grid of occlusion coefficients in [0,1]
   * @param startX
   *   source x (cell space)
   * @param startY
   *   source y (cell space)
   * @param radius
   *   max distance (cells)
   * @return
   *   row-major (x-fast) flattened array sized width*height with values in [0,1]
   */
  def compute(
      OcclusionGrid: Grid[Double],
  )(startX: Int, startY: Int, radius: Double): ArraySeq[Double]
