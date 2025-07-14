package io.github.srs.model

trait Orientation:
  def degrees: Double
  def toRadians: Double

object Orientation:
  def apply(deg: Double): Orientation = OrientationImpl(deg)

  private case class OrientationImpl(private val deg: Double) extends Orientation:
    override def degrees: Double = deg
    override def toRadians: Double = math.toRadians(deg)