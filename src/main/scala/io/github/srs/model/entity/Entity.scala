package io.github.srs.model.entity

import scala.annotation.tailrec

import io.github.srs.model.entity.ShapeType.Rectangle
import io.github.srs.model.entity.{ Orientation, ShapeType }
import io.github.srs.utils.collision.Collision.isColliding

/**
 * Represents a generic entity in a two-dimensional space.
 *
 * An entity is characterized by its position, shape, and orientation.
 */
trait Entity:
  /**
   * The position of the entity in 2D space.
   *
   * @return
   *   a [[Point2D]] representing the (x, y) coordinates of this entity.
   */
  def position: Point2D

  /**
   * The shape type of the entity.
   *
   * @return
   *   the [[ShapeType]] that defines the geometric shape of this entity.
   */
  def shape: ShapeType

  /**
   * The orientation of the entity in 2D space.
   *
   * @return
   *   an [[Orientation]] representing the angle or facing direction of this entity.
   */
  def orientation: Orientation
end Entity

object Entity:

  extension (e: Entity)

    /**
     * Checks if this entity collides with another entity.
     *
     * @param other
     *   the other entity to check for collision against this entity.
     * @return
     *   true if the two entities collide, false otherwise.
     */
    @tailrec
    def collidesWith(other: Entity): Boolean =
      import Point2D.*
      (e.shape, other.shape) match
        case (ShapeType.Circle(eRadius), ShapeType.Circle(otherRadius)) =>
          e.position.distanceTo(other.position) <= (eRadius + otherRadius)
        case (ShapeType.Rectangle(w, h), ShapeType.Rectangle(_, _)) =>
          isColliding(e.position, Rectangle(w, h), e.orientation)(
            other.position,
            other.shape,
            other.orientation,
          )
        case (ShapeType.Circle(_), ShapeType.Rectangle(_, _)) => other.collidesWith(e)
        case (ShapeType.Rectangle(w, h), ShapeType.Circle(_)) =>
          isColliding(e.position, Rectangle(w, h), e.orientation)(
            other.position,
            other.shape,
            other.orientation,
          )
  end extension
end Entity
