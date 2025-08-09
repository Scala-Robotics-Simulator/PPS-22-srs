package io.github.srs.model.lighting.grid

/**
 * Represents the size of a sub grid in terms of width and height.
 *
 * @param width
 *   The width of the sub grid.
 * @param height
 *   The height of the sub grid.
 */
final case class SubGridSize(width: Int, height: Int) derives CanEqual

/**
 * Represents a sub-cell within the sub grid.
 *
 * @param x
 *   The x-coordinate of the sub-cell.
 * @param y
 *   The y-coordinate of the sub-cell.
 */
final case class SubCell(x: Int, y: Int) derives CanEqual

object SubGridIndexing:

  /**
   * Clamps a value to a specified range.
   *
   * @param v
   *   The value to clamp.
   * @param minInclusive
   *   The minimum value of the range (inclusive).
   * @param maxInclusive
   *   The maximum value of the range (inclusive).
   * @return
   *   The clamped value, which is guaranteed to be within the specified range.
   */
  inline private def clampToRange(v: Int, minInclusive: Int, maxInclusive: Int): Int =
    if v < minInclusive then minInclusive
    else if v > maxInclusive then maxInclusive
    else v

  /**
   * Calculates the size of the sub grid based on the raw dimensions and the number of subdivisions per cell.
   *
   * @param rawWidth
   *   The width of the grid in coarse units.
   * @param rawHeight
   *   The height of the grid in coarse units.
   * @param subdivisionsPerCell
   *   The number of subdivisions per cell.
   * @return
   *   A [[SubGridSize]] representing the dimensions of the sub grid.
   */
  inline def toSubGridSize(rawWidth: Int, rawHeight: Int, subdivisionsPerCell: Int): SubGridSize =
    SubGridSize(rawWidth * subdivisionsPerCell, rawHeight * subdivisionsPerCell)

  /**
   * Converts world/environment coordinates to sub-cell indices in the sub grid.
   *
   * @param x
   *   The x-coordinate in coarse units.
   * @param y
   *   The y-coordinate in coarse units.
   * @param size
   *   The size of the sub grid.
   * @param subdivisionsPerCell
   *   The number of subdivisions per cell.
   * @return
   *   A [[SubCell]] representing the sub-grid indices.
   */
  def coordinatesToSubCell(x: Double, y: Double, size: SubGridSize, subdivisionsPerCell: Int): SubCell =
    val fx = clampToRange(math.round(x * subdivisionsPerCell).toInt, 0, size.width - 1)
    val fy = clampToRange(math.round(y * subdivisionsPerCell).toInt, 0, size.height - 1)
    SubCell(fx, fy)

  /**
   * Calculates the sub-cell center position in environment/world coordinates.
   *
   * @param sc
   *   The sub-cell for which to calculate the center position.
   * @param subdivisionsPerCell
   *   The number of subdivisions per cell.
   * @return
   *   A tuple [[(Double, Double)]] representing the x and y coordinates of the sub-cell center in coarse units.
   */
  inline def subCellCenterInCoordinates(sc: SubCell, subdivisionsPerCell: Int): (Double, Double) =
    ((sc.x + 0.5) / subdivisionsPerCell, (sc.y + 0.5) / subdivisionsPerCell)
end SubGridIndexing
