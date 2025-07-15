package io.github.srs.model.entity.dynamic

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }

trait DynamicEntity:
  def position: Point2D
  def shape: ShapeType
  def orientation: Orientation
  def actuators: Option[Seq[Actuator]]
