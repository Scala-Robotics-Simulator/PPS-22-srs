package io.github.srs.model.illumination.model

import scala.collection.immutable.ArraySeq

import io.github.srs.model.entity.Point2D

/**
 * Light field representing illumination values in a grid.
 *
 * @param dims
 *   Dimensions of the grid.
 * @param data
 *   A flat ArraySeq containing the illumination values, indexed row-major.
 */

final case class LightField private (dims: GridDims, data: ArraySeq[Double]) derives CanEqual:

  /**
   * Sample illumination at world coordinates with bilinear interpolation.
   *
   * @param pos
   *   Position in world coordinates (meters).
   * @param scale
   *   Scale factor (implicit).
   * @return
   *   The interpolated illumination value at the given world position.
   */
  def illuminationAt(pos: Point2D)(using scale: ScaleFactor): Double =
    val (worldX, worldY) = pos

    val gridX = worldX * scale.toDouble
    val gridY = worldY * scale.toDouble
    if worldX < 0 || worldY < 0 then 0.0
    else bilinearInterpolate(gridX, gridY)

  /**
   * Performs bilinear interpolation to compute a value at the given grid coordinates.
   *
   * @param gridX
   * The x-coordinate in the grid where interpolation is to be performed.
   * @param gridY
   * The y-coordinate in the grid where interpolation is to be performed.
   * @return
   * The interpolated value at the specified grid coordinates.
   */
  private def bilinearInterpolate(gridX: Double, gridY: Double): Double =
    val xBase = gridX.floor.toInt
    val yBase = gridY.floor.toInt
    val fracX = gridX - xBase
    val fracY = gridY - yBase

    def valueAt(x: Int, y: Int): Double =
      if dims.inBounds(x, y) then data.lift(dims.toIndex(x, y)).getOrElse(0.0)
      else 0.0

    val topLeft = valueAt(xBase, yBase)
    val topRight = valueAt(xBase + 1, yBase)
    val bottomLeft = valueAt(xBase, yBase + 1)
    val bottomRight = valueAt(xBase + 1, yBase + 1)

    val interTop = Math.fma(topRight - topLeft, fracX, topLeft)
    val interBottom = Math.fma(bottomRight - bottomLeft, fracX, bottomLeft)

    Math.fma(interBottom - interTop, fracY, interTop)

  /**
   * Check if the field is empty (all zeros).
   */
  lazy val isIlluminated: Boolean = data.exists(_ > 0.0)

end LightField

object LightField:

  /**
   * Smart constructor that validates data size.
   *
   * @param dims
   *   Dimensions of the grid.
   * @param data
   *   ArraySeq of illumination values.
   * @return
   *   A LightField with correctly sized data; if size mismatches, returns an empty field.
   */

  def apply(dims: GridDims, data: ArraySeq[Double]): LightField =
    if data.lengthIs == dims.totalCells then new LightField(dims, data)
    else new LightField(dims, ArraySeq.fill(dims.totalCells)(0.0))

  /**
   * Create an empty (all-zeros) light field.
   *
   * @param dims
   *   Dimensions of the grid.
   * @return
   *   A LightField filled with zeros.
   */

  def empty(dims: GridDims): LightField =
    new LightField(dims, ArraySeq.fill(dims.totalCells)(0.0))

  /**
   * Create a uniformly lit field.
   *
   * @param dims
   *   Dimensions of the grid.
   * @param intensity
   *   Illumination value to use (clamped between 0.0 and 1.0).
   * @return
   *   LightField filled with the uniform value.
   */

  def uniform(dims: GridDims)(intensity: Double): LightField =
    val clamped = math.max(0.0, math.min(1.0, intensity))
    new LightField(dims, ArraySeq.fill(dims.totalCells)(clamped))

end LightField
