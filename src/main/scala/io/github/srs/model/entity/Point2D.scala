package io.github.srs.model.entity

import io.github.srs.model.Cell

/**
 * Represents a point in a two-dimensional Cartesian coordinate system.
 *
 * A `Point2D` is defined as a tuple of two `Double` values: `(x, y)`.
 */
type Point2D = (Double, Double)

/**
 * Companion object for [[Point2D]], providing convenient factory methods.
 */
object Point2D:

  /**
   * Creates a new [[Point2D]] instance given x and y coordinates.
   *
   * @param x
   *   the x-coordinate of the point.
   * @param y
   *   the y-coordinate of the point.
   * @return
   *   a new [[Point2D]] representing the given coordinates.
   */
  def apply(x: Double, y: Double): Point2D = (x, y)

  /**
   * Extension methods for [[Point2D]], providing additional geometric operations.
   *
   * These methods enrich `Point2D` with utility functions.
   */
  extension (p: Point2D)
    /**
     * The x-coordinate of this point.
     */
    def x: Double = p._1

    /**
     * The y-coordinate of this point.
     */
    def y: Double = p._2

    /**
     * Adds two points together, resulting in a new point whose coordinates are the sum of the respective coordinates of
     * the two points.
     * @param other
     *   the other point to add.
     * @return
     *   a new [[Point2D]] representing the sum of this point and `other`.
     */
    infix def +(other: Point2D): Point2D =
      Point2D(p.x + other.x, p.y + other.y)

    /**
     * Subtracts another point from this point, resulting in a new point whose coordinates are the difference of the
     * respective coordinates of the two points.
     * @param other
     *   the other point to subtract.
     * @return
     *   a new [[Point2D]] representing the difference of this point and `other`.
     */
    infix def -(other: Point2D): Point2D =
      Point2D(p.x - other.x, p.y - other.y)

    /**
     * Multiplies this point by a scalar value, resulting in a new point whose coordinates are the product of the
     * respective coordinates of this point and the scalar.
     * @param scalar
     *   scalar value to multiply the point by.
     * @return
     *   a new [[Point2D]] representing the scaled point.
     */
    infix def *(scalar: Double): Point2D =
      Point2D(p.x * scalar, p.y * scalar)

    /**
     * Calculates the dot product of this point with another point.
     * @param other
     *   the other point to compute the dot product with.
     * @return
     *   the dot product as a `Double`, which is the sum of the products of the corresponding coordinates.
     */
    infix def dot(other: Point2D): Double =
      p.x * other.x + p.y * other.y

    /**
     * Calculates the magnitude (length) of this point vector.
     * @return
     *   the magnitude as a `Double`, which is the square root of the sum of the squares of the coordinates.
     */
    def magnitude: Double =
      math.sqrt(p.x * p.x + p.y * p.y)

    /**
     * Normalizes this point to a unit vector.
     * @return
     *   a new [[Point2D]] representing the normalized point.
     */
    def normalize: Point2D =
      val mag = p.magnitude
      if math.abs(mag) < 1e-10 then p else Point2D(p.x / mag, p.y / mag)

    /**
     * Computes the Euclidean distance between this point and another point.
     *
     * The Euclidean distance is calculated as:
     * {{{
     * sqrt((x2 - x1)^2 + (y2 - y1)^2)
     * }}}
     *
     * @param other
     *   the other point to which the distance is measured.
     * @return
     *   the Euclidean distance between this point and `other`.
     */
    def distanceTo(other: Point2D): Double =
      math.sqrt(math.pow(other.x - p.x, 2) + math.pow(other.y - p.y, 2))

    /**
     * Converts this point to a [[Cell]] by rounding its coordinates.
     *
     * The x and y coordinates of the point are rounded to the nearest integer to create a new cell in the grid.
     *
     * @return
     *   a new [[Cell]] with the rounded coordinates of this point
     */
    def toCell: Cell = Cell(p.x.round.toInt, p.y.round.toInt)

  end extension
end Point2D
