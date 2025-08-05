package io.github.srs.utils.geometry2d

import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.utils.*

/**
 * Represents a 2D vector in the collision system.
 */
type Vector2D = Point2D

/**
 * Companion object for Vector2D, providing utility methods for vector operations.
 */
object Vector2D:

  /**
   * Creates a new Vector2D instance from the given x and y coordinates.
   * @param x
   *   the x-coordinate of the vector
   * @param y
   *   the y-coordinate of the vector
   * @return
   *   a new Vector2D instance representing the vector in 2D space
   */
  def apply(x: Double, y: Double): Vector2D = Point2D(x, y)

  extension (v: Vector2D)

    /**
     * Rotates the vector by the specified angle.
     * @param angle
     *   the angle to rotate the vector by
     * @return
     *   a new Vector2D instance representing the rotated vector
     */
    def rotate(angle: Orientation): Vector2D =
      import Point2D.*
      val radians = angle.toRadians
      val cos = Math.cos(radians)
      val sin = Math.sin(radians)
      Point2D(
        v.x * cos - v.y * sin,
        v.x * sin + v.y * cos,
      )

    /**
     * Projects the vector onto a line defined by its origin and direction.
     * @param line
     *   the line onto which the vector will be projected
     * @return
     *   a new Vector2D instance representing the projection of the vector onto the line
     */
    def project(line: Line): Vector2D =
      import Point2D.*
      val lineDirection = line.direction
      val dotProduct = line.direction.x * (v.x - line.origin.x) + line.direction.y * (v.y - line.origin.y)
      Vector2D(
        line.origin.x + (dotProduct * lineDirection.x),
        line.origin.y + (dotProduct * lineDirection.y),
      )
  end extension
end Vector2D
