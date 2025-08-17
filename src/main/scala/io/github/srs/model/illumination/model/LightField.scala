package io.github.srs.model.illumination.model

import scala.collection.immutable.ArraySeq

import io.github.srs.model.entity.Point2D
import io.github.srs.model.illumination.model.GridDims

/**
 * Represents a Light field data for an environment at a particular grid density, saturated to [0,1].
 *
 * @param dims
 *   The dimensions of the grid in cells, derived from the environment and scale factor.
 * @param data
 *   The light intensity values stored in a row-major order (x fast) flattened array.
 */
final case class LightField(dims: GridDims, data: ArraySeq[Double]) derives CanEqual:

  /**
   * Gets the width of the grid in cells.
   *
   * @return
   *   The number of cells along the width of the grid.
   */
  inline def width: Int = dims.widthCells

  /**
   * Gets the height of the grid in cells.
   *
   * @return
   *   The number of cells along the height of the grid.
   */
  inline def height: Int = dims.heightCells

  /**
   * Computes the index in the flattened data array for a given cell coordinate (x, y).
   *
   * @param x
   *   The x-coordinate of the cell.
   * @param y
   *   The y-coordinate of the cell.
   * @return
   *   The index in the flattened data array.
   */
  inline private def idx(x: Int, y: Int): Int = y * width + x

  /**
   * Bilinear sample at a world position. Out-of-bounds samples return 0.0.
   *
   * @param p
   *   The point in world coordinates.
   * @param s
   *   The scale factor used to convert world coordinates to cell coordinates.
   * @return
   *   The interpolated light intensity at the given point.
   */
  def sampleAtWorld(p: Point2D)(using s: ScaleFactor): Double =
    val (fx, fy) = (p._1 * s.toDouble, p._2 * s.toDouble)
    val (x0, y0) = (math.floor(fx).toInt, math.floor(fy).toInt)
    val (tx, ty) = (fx - x0, fy - y0)

    def at(x: Int, y: Int): Double =
      if x >= 0 && y >= 0 && x < width && y < height then data.lift(idx(x, y)).getOrElse(0.0)
      else 0.0

    val (i00, i10, i01, i11) = (at(x0, y0), at(x0 + 1, y0), at(x0, y0 + 1), at(x0 + 1, y0 + 1))
    val (a, b) = (i00 + (i10 - i00) * tx, i01 + (i11 - i01) * tx)
    a + (b - a) * ty
end LightField
