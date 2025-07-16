package io.github.srs.model.entity.dynamic

trait Wheel:
  def speed: Double
  def updated(to: Double): Wheel

object Wheel:
  def apply(speed: Double): Wheel = new WheelImpl(speed)

  private class WheelImpl(val speed: Double) extends Wheel:
    def updated(to: Double): Wheel = Wheel(to)
