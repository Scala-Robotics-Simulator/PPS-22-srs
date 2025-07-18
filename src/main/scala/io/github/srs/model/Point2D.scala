package io.github.srs.model
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

  val Zero: Point2D = (0.0, 0.0)

/**
 * Extension methods for [[Point2D]], providing additional geometric operations.
 *
 * These methods enrich `Point2D` with utility functions.
 */
extension (p: Point2D)
  /**
   * The x-coordinate of this point.
   */
  private def x: Double = p._1

  /**
   * The y-coordinate of this point.
   */
  private def y: Double = p._2

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

  /** transforms the [[Point2D]] into a [[Cell]].
   * The coordinates are rounded to the nearest integer values.
   * @return
   *  a [[Cell]] representing the point's coordinates.
   * */
  def toCell: Cell = Cell(p.x.round.toInt, p.y.round.toInt)

end extension
