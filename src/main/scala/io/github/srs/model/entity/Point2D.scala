package io.github.srs.model.entity

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

    infix def +(other: Point2D): Point2D =
      Point2D(p.x + other.x, p.y + other.y)

    infix def -(other: Point2D): Point2D =
      Point2D(p.x - other.x, p.y - other.y)

    infix def *(scalar: Double): Point2D =
      Point2D(p.x * scalar, p.y * scalar)

    infix def dot(other: Point2D): Double =
      p.x * other.x + p.y * other.y

    def magnitude: Double =
      math.sqrt(p.x * p.x + p.y * p.y)

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

  end extension
end Point2D
