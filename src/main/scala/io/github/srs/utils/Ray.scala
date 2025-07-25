package io.github.srs.utils

import io.github.srs.model.entity.{ Entity, Point2D, ShapeType }

object Ray:

  opaque type Line = (p1: Point2D, p2: Point2D)

  object Line:
    def apply(p1: Point2D, p2: Point2D): Line = (p1, p2)

    def unapply(line: Line): Option[(Point2D, Point2D)] = Some(line)

  def intersectRay(entity: Entity, origin: Point2D, end: Point2D): Option[Double] =

    def intersectRayWithLine(line: Line, origin: Point2D, end: Point2D): Option[Double] =
      import Point2D.*
      val (p1, p2) = line
      val dir = end - origin
      val lineDir = p2 - p1
      val denom = dir.x * lineDir.y - dir.y * lineDir.x
      if math.abs(denom) < 1e-10 then None // Lines are parallel
      else
        val t = ((p1.x - origin.x) * lineDir.y - (p1.y - origin.y) * lineDir.x) / denom
        if t < 0 then None else Some(t * dir.magnitude)

    entity.shape match
      case ShapeType.Circle(radius) =>
        import Point2D.*
        val dir = end - origin
        val f = origin - entity.position
        val a = dir dot dir
        val b = 2 * (f dot dir)
        val c = (f dot f) - radius * radius

        val discriminant = b * b - 4 * a * c

        if discriminant < -1e-10 then None
        else
          val sqrtDisc = math.sqrt(discriminant)
          val t1 = (-b - sqrtDisc) / (2 * a)
          val t2 = (-b + sqrtDisc) / (2 * a)

          val validTs = Seq(t1, t2).filter(_ >= 0)
          validTs.minOption.map(_ * dir.magnitude)
      case ShapeType.Rectangle(width, height) =>
        import Point2D.*
        val topLineStart = Point2D(entity.position.x + width / 2, entity.position.y + height / 2)
        val topLineEnd = Point2D(entity.position.x - width / 2, entity.position.y + height / 2)
        val topLine = Line(topLineStart, topLineEnd)
        val bottomLineStart = Point2D(entity.position.x - width / 2, entity.position.y - height / 2)
        val bottomLineEnd = Point2D(entity.position.x + width / 2, entity.position.y - height / 2)
        val bottomLine = Line(bottomLineStart, bottomLineEnd)
        val leftLineStart = topLineEnd
        val leftLineEnd = bottomLineStart
        val leftLine = Line(leftLineStart, leftLineEnd)
        val rightLineStart = topLineStart
        val rightLineEnd = bottomLineEnd
        val rightLine = Line(rightLineStart, rightLineEnd)
        val lines = Seq(topLine, bottomLine, leftLine, rightLine)
        lines.flatMap(line => intersectRayWithLine(line, origin, end)).minOption
    end match
  end intersectRay
end Ray
