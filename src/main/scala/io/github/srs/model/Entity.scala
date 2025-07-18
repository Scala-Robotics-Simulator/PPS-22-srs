package io.github.srs.model

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
