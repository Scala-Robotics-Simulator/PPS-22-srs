package io.github.srs.model.entity

import scala.annotation.tailrec

import io.github.srs.model.entity.ShapeType.Rectangle
import io.github.srs.model.entity.{ Orientation, ShapeType }
import io.github.srs.utils.collision.Collision.isColliding
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.DomainError
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.validate
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.validate
import io.github.srs.model.entity.staticentity.dsl.LightDsl.validate
import io.github.srs.model.entity.staticentity.dsl.BoundaryDsl.validate

/**
 * Represents a generic entity in a two-dimensional space.
 *
 * An entity is characterized by its position, shape, and orientation.
 */
trait Entity:

  /**
   * The unique identifier for the entity.
   *
   * @return
   *   a unique identifier UUID.
   */
  def id: java.util.UUID

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

  /**
   * Validates an entity to ensure it meets the domain constraints.
   *
   * @param e
   *   the entity to validate.
   * @return
   *   [[Right]] if the entity is valid, or [[Left]] with a validation error.
   */
  def validateEntity(e: Entity): Validation[Entity] =
    e match
      case se: StaticEntity =>
        se match
          case o: StaticEntity.Obstacle => o.validate
          case l: StaticEntity.Light => l.validate
          case b: StaticEntity.Boundary => b.validate
      case de: DynamicEntity =>
        de match
          case r: Robot => r.validate
          case de => Right[DomainError, DynamicEntity](de)
      case _ => Right[DomainError, Entity](e)

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
