package io.github.srs.model.entity.staticentity.dsl

import io.github.srs.model.entity.staticentity.StaticEntity.Boundary
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.positiveWithZero

/**
 * The DSL for creating and configuring boundaries in the simulation.
 */
object BoundaryDsl:

  /**
   * Creates a new boundary with default properties.
   * @return
   *   A new instance of [[Boundary]] with default values.
   */
  def boundary: Boundary = Boundary()

  /**
   * Provides an extension method for the Boundary class to allow for a more fluent DSL.
   */
  extension (boundary: Boundary)

    /**
     * Sets the unique identifier for the boundary.
     *
     * @param id
     *   the unique identifier for the boundary.
     * @return
     *   the updated boundary with the specified identifier.
     */
    infix def withId(id: java.util.UUID): Boundary =
      boundary.copy(id = id)

    /**
     * Sets the position of the boundary.
     * @param pos
     *   the position of the boundary.
     * @return
     *   The updated boundary with the specified position.
     */
    infix def at(pos: Point2D): Boundary = boundary.copy(pos = pos)

    /**
     * Sets the orientation of the boundary.
     * @param orientation
     *   the orientation of the boundary.
     * @return
     *   The updated boundary with the specified orientation.
     */
    infix def withOrientation(orientation: Orientation): Boundary = boundary.copy(orient = orientation)

    /**
     * Sets the width of the boundary.
     * @param width
     *   the width of the boundary.
     * @return
     *   The updated boundary with the specified width.
     */
    infix def withWidth(width: Double): Boundary = boundary.copy(width = width)

    /**
     * Sets the height of the boundary.
     * @param height
     *   the height of the boundary.
     * @return
     *   The updated boundary with the specified height.
     */
    infix def withHeight(height: Double): Boundary = boundary.copy(height = height)

    /**
     * Validates the boundary's properties. This method allows ensuring that the boundary properties reflect domain
     * constraints.
     * @return
     *   [[Right]] with the updated [[Boundary]] if valid, otherwise [[Left]] with a validation error.
     */
    infix def validate: Validation[Boundary] =
      for
        width <- positiveWithZero("width", boundary.width)
        height <- positiveWithZero("height", boundary.height)
      yield boundary.copy(width = width, height = height)
  end extension
end BoundaryDsl
