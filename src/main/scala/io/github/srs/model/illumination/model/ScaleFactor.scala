package io.github.srs.model.illumination.model

import io.github.srs.model.entity.Point2D
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.bounded

/**
 * Represents a scale factor (in cells per meter) used to convert world coordinates
 */
opaque type ScaleFactor = Int

/**
 * Companion object providing utility methods and extensions for working with scale factors.
 */
object ScaleFactor:

  /**
   * Validate and build a [[ScaleFactor]] in the inclusive range [1,1000].
   *
   * @param n
   *   The integer value to validate as a scale factor.
   * @return
   *   A [[Validation]] result containing the validated [[ScaleFactor]] or an error.
   */
  def validate(n: Int): Validation[ScaleFactor] =
    bounded("ScaleFactor", n, 1, 1000, includeMax = true).map(v => v: ScaleFactor)

  /**
   * Extension methods for [[ScaleFactor]] providing utility functions.
   */
  extension (s: ScaleFactor)
    /**
     * Converts a radius in meters to the corresponding number of cells.
     *
     * @param m
     *   The radius in meters.
     * @return
     *   The radius in cells, rounded up to the nearest integer.
     */
    inline def radiusToCells(m: Double): Int = math.ceil(m * s.toDouble).toInt

    /**
     * Converts a point in world coordinates to the corresponding floored cell coordinates.
     *
     * @param p
     *   The point in world coordinate¶.
     * @return
     *   A tuple (Int, Int) representing the cell coordinates.
     */
    inline def point2DToCellFloor(p: Point2D): (Int, Int) =
      (math.floor(p._1 * s.toDouble).toInt, math.floor(p._2 * s.toDouble).toInt)

  end extension

  /**
   * Implicit conversion from [[ScaleFactor]] to `Int`.
   */
  given sfToInt: Conversion[ScaleFactor, Int] with
    inline def apply(s: ScaleFactor): Int = s

  /**
   * Implicit conversion from `Int` to [[ScaleFactor]].
   */
  given CanEqual[ScaleFactor, ScaleFactor] = CanEqual.derived
end ScaleFactor

/**
 * Exports selected members of the `ScaleFactor` object for easier access.
 */
export ScaleFactor.{ point2DToCellFloor, radiusToCells, validate }
