package io.github.srs.model.entity.dynamic

import io.github.srs.model.entity.ShapeType

trait Wheel:
  def speed: Double
  def shape: ShapeType.Circle
  def updated(to: Double): Wheel

object Wheel:
  def apply(speed: Double, shape: ShapeType.Circle): Wheel = new WheelImpl(speed, shape)

  private class WheelImpl(val speed: Double, val shape: ShapeType.Circle) extends Wheel:
    override def updated(to: Double): Wheel = Wheel(to, shape)
