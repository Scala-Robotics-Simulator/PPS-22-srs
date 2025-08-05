package io.github.srs.utils.geometry2d

import io.github.srs.model.entity.Point2D
import io.github.srs.utils.*

/**
 * Represents a line in the collision system, defined by an origin point and a direction vector.
 */
type Line = (origin: Point2D, direction: Point2D)

/**
 * Companion object for Line, providing utility methods for creating and manipulating lines.
 */
object Line:

  /**
   * Creates a new Line instance from the given origin and direction points.
   * @param origin
   *   the starting point of the line
   * @param direction
   *   the direction vector of the line
   * @return
   *   a new Line instance representing the line in 2D space
   */
  def apply(origin: Point2D, direction: Point2D): Line = (origin, direction)
