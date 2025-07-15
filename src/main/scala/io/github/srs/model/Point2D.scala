package io.github.srs.model

/**
 * Represents a 2D point.
 */
type Point2D = (Double, Double)

/**
 * Companion object for Point2D, providing an apply method to create instances.
 */
object Point2D:
  def apply(x: Double, y: Double): Point2D = (x, y)

/**
 * Extension methods for Point2D to provide additional functionality.
 */
extension (p: Point2D)
  private def x: Double = p._1
  private def y: Double = p._2

  /**
   * Calculates the Euclidean distance from this point to another point.
   * @param other
   *   the other point to which the distance is calculated.
   * @return
   *   the Euclidean distance between the two points.
   */
  def distanceTo(other: Point2D): Double =
    math.sqrt(math.pow(other.x - p.x, 2) + math.pow(other.y - p.y, 2))
