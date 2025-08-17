package io.github.srs.model.illumination.model

import io.github.srs.model.entity.Point2D

/**
 * Object containing utility methods for working with cells in a grid-based environment.
 */
object Cell:

  /**
   * Convert world coordinates to cell (floored) coordinates using the current scale
   *
   * @param p
   *   The point in world coordinates.
   * @param s
   *   The scale factor used to convert world coordinates to cell coordinates.
   * @return
   *   A tuple (Int, Int) representing the cell coordinates.
   */
  inline def toCellFloor(p: Point2D)(using s: ScaleFactor): (Int, Int) =
    s.point2DToCellFloor(p)

  /**
   * Convert a world-space radius (meters) to a radius in cells
   *
   * @param radius
   *   The radius in world space (meters).
   * @param s
   *   The scale factor used to convert world space to cell space.
   * @return
   *   The radius in cells.
   */
  inline def radiusCells(radius: Double)(using s: ScaleFactor): Int =
    s.radiusToCells(radius)
end Cell
