package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.ShapeType

/**
 * Represents a wheel with linear speed and circular shape.
 *
 * @param speed
 *   the linear speed of the wheel (in meters per second).
 * @param shape
 *   the physical shape of the wheel, assumed to be a circle.
 */
final case class Wheel(speed: Double, shape: ShapeType.Circle):
  /**
   * Returns a new instance of this wheel with an updated linear speed.
   *
   * @param to
   *   the new linear speed (in meters per second).
   * @return
   *   a new [[Wheel]] instance with the updated speed.
   */
  def updatedSpeed(to: Double): Wheel = copy(speed = to)
