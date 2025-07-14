package io.github.srs.model

trait Entity:
  def position: Point2D
  def shape: ShapeType
  def orientation: Orientation