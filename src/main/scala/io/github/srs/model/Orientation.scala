package io.github.srs.model

trait Orientation:
  def degrees: Double
  def toRadians: Double

object Orientation:
  def apply(degree: Double): Orientation = OrientationImpl(degree)

  private case class OrientationImpl(private val degree: Double) extends Orientation:
    override def degrees: Double = degree
    override def toRadians: Double = math.toRadians(degree)
