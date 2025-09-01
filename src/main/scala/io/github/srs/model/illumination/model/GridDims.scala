package io.github.srs.model.illumination.model

import io.github.srs.model.environment.Environment

/**
 * Grid dimensions for illumination calculations
 */
final case class GridDims(widthCells: Int, heightCells: Int) derives CanEqual:

  /**
   * Total number of cells in the grid
   */
  def totalCells: Int = widthCells * heightCells

  /**
   * Check if a cell coordinate is within bounds
   */
  def inBounds(x: Int, y: Int): Boolean =
    x >= 0 && x < widthCells && y >= 0 && y < heightCells

  /**
   * Convert 2D coordinates to linear index.
   * @param x
   *   X coordinate
   * @param y
   *   Y coordinate
   * @return
   *   Linear index
   */
  def toIndex(x: Int, y: Int): Int = y * widthCells + x

  /**
   * Convert linear index to 2D coordinates.
   * @param index
   *   Linear index
   * @return
   *   (X, Y) coordinates
   */
  def fromIndex(index: Int): (Int, Int) = (index % widthCells, index / widthCells)

end GridDims

object GridDims:

  /**
   * Create grid dimensions from environment and scale factor.
   *
   * @param env
   *   [[io.github.srs.model.environment.Environment]] to base dimensions on
   * @param scale
   *   Scale factor to apply
   */
  def from(env: Environment)(scale: ScaleFactor): GridDims =
    GridDims(
      widthCells = math.max(0, env.width * scale),
      heightCells = math.max(0, env.height * scale),
    )
