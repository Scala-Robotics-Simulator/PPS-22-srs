package io.github.srs.model.entity.staticentity.dsl

import java.util.UUID

import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.positive

/**
 * The DSL for creating and configuring obstacles in the simulation.
 */
object ObstacleDsl:

  /**
   * Creates a new obstacle with default properties.
   * @return
   *   A new instance of [[Obstacle]] with default values.
   */
  def obstacle: Obstacle = Obstacle()

  extension (obstacle: Obstacle)

    /**
     * Sets the unique identifier for the obstacle.
     *
     * @param id
     *   the unique identifier for the obstacle.
     * @return
     *   the updated obstacle with the specified identifier.
     */
    infix def withId(id: UUID): Obstacle =
      obstacle.copy(id = id)

    /**
     * Sets the position of the obstacle.
     * @param pos
     *   the position of the obstacle.
     * @return
     *   The updated obstacle with the specified position.
     */
    infix def at(pos: Point2D): Obstacle = obstacle.copy(pos = pos)

    /**
     * Sets the orientation of the obstacle.
     * @param orientation
     *   the orientation of the obstacle.
     * @return
     *   The updated obstacle with the specified orientation.
     */
    infix def withOrientation(orientation: Orientation): Obstacle = obstacle.copy(orient = orientation)

    /**
     * Sets the width of the obstacle.
     * @param width
     *   the width of the obstacle.
     * @return
     *   The updated obstacle with the specified width.
     */
    infix def withWidth(width: Double): Obstacle = obstacle.copy(width = width)

    /**
     * Sets the height of the obstacle.
     * @param height
     *   the height of the obstacle.
     * @return
     *   The updated obstacle with the specified height.
     */
    infix def withHeight(height: Double): Obstacle = obstacle.copy(height = height)

    /**
     * Validates the obstacle's properties. This method allows ensuring that the obstacle properties reflect domain
     * constraints.
     * @return
     *   [[Right]] if the obstacle is valid, otherwise [[Left]] with an error message.
     */
    infix def validate: Validation[Obstacle] =
      for
        w <- positive("width", obstacle.width)
        h <- positive("height", obstacle.height)
      yield obstacle.copy(width = w, height = h)
  end extension
end ObstacleDsl
