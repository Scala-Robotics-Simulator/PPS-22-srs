package io.github.srs.model

type Point2D = (Double, Double)

object Point2D:
  def apply(x: Double, y: Double): Point2D = (x, y)

extension (p: Point2D)
  private def x: Double = p._1
  private def y: Double = p._2

  def distanceTo(other: Point2D): Double =
    math.sqrt(math.pow(other.x - p.x, 2) + math.pow(other.y - p.y, 2))
