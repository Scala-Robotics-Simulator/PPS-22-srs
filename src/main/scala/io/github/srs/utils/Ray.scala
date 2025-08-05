package io.github.srs.utils

import io.github.srs.model.entity.{ Entity, Point2D, ShapeType }
import io.github.srs.utils.geometry2d.{ Line, Vector2D }

object Ray:

  /**
   * Calculates the intersection of a ray defined by an origin and an end point with an entity.
   * @param entity
   *   The entity to check for intersection.
   * @param origin
   *   The starting point of the ray.
   * @param end
   *   The end point of the ray.
   * @note
   *   The ray is defined as a line segment from `origin` to `end`. The function checks if this ray intersects with the
   *   entity's shape. If the entity is a circle, it checks for intersection with the circle's perimeter. If the entity
   *   is a rectangle, it checks for intersection with the rectangle's edges. The function returns the distance from the
   *   origin to the intersection point along the ray, if an intersection occurs. If there is no intersection, it
   *   returns `None`.
   * @return
   *   An `Option[Double]` representing the distance from the origin to the intersection point along the ray. If there
   *   is no intersection, it returns `None`.
   */
  def intersectRay(entity: Entity, origin: Point2D, end: Point2D): Option[Double] =

    /**
     * Computes the intersection between a ray and a line segment.
     *
     * The ray is defined from `origin` to `end`, and the segment is defined by two endpoints `p1` and `p2`. This
     * function uses the parametric form of both the ray and the segment to determine the intersection point.
     *
     * Geometry:
     *   - Ray: `R(t) = origin + t * dir`, with `t >= 0`
     *   - Segment: `S(u) = p1 + u * segDir`, with `0 <= u <= 1`
     *
     * Where:
     *   - `dir = end - origin` is the ray direction
     *   - `segDir = p2 - p1` is the segment direction
     *   - The denominator of the system is: `denom = dir.x * segDir.y - dir.y * segDir.x`
     *
     * The system solves:
     * {{{
     * t = (dx * segDir.y - dy * segDir.x) / denom
     * u = (dx * dir.y - dy * dir.x) / denom
     * }}}
     *
     * Conditions for valid intersection:
     *   - `t >= 0` (along the ray)
     *   - `0 <= u <= 1` (within the segment)
     *
     * @param line
     *   The line segment to test, represented by two points.
     * @return
     *   Some(distance) from the ray origin to the intersection point, or None if there is no intersection.
     */
    def intersectRayWithLine(line: Line): Option[Double] =
      import Point2D.*
      val (p1, p2) = line
      val dir = end - origin
      val segDir = p2 - p1

      val denom = dir.x * segDir.y - dir.y * segDir.x
      if math.abs(denom) < 1e-10
      then None // Parallel
      else
        val dx = p1.x - origin.x
        val dy = p1.y - origin.y

        val t = (dx * segDir.y - dy * segDir.x) / denom
        val u = (dx * dir.y - dy * dir.x) / denom

        if t >= 0 && u >= 0 && u <= 1
        then Some(t * dir.magnitude)
        else None

    /**
     * Computes the intersection between a ray and a circle centered at the entity's position.
     *
     * The ray is defined from `origin` to `end`. The circle is centered at `entity.position` with the given `radius`.
     * This function solves the quadratic equation for the parametric form of the ray and the implicit equation of the
     * circle.
     *
     * Geometry:
     *   - Ray: `R(t) = origin + t * dir`, with `t >= 0`
     *   - Circle: `||R(t) - center||^2 = r^2`
     *
     * Define:
     *   - `f = origin - center`
     *   - `a = dir ⋅ dir`
     *   - `b = 2 * (f ⋅ dir)`
     *   - `c = (f ⋅ f) - radius^2`
     *
     * Solves the quadratic equation:
     * {{{
     * a * t^2 + b * t + c = 0
     * }}}
     *
     * The discriminant is:
     * {{{
     * Δ = b^2 - 4ac
     * }}}
     *
     * If the discriminant is non-negative, the function returns the smallest non-negative root scaled by `|dir|`.
     *
     * @param radius
     *   The radius of the circle.
     * @return
     *   Some(distance) from the ray origin to the intersection point, or None if there is no intersection.
     */
    def intersectRayWithCircle(radius: Double) =
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

    entity.shape match
      case ShapeType.Circle(radius) =>
        intersectRayWithCircle(radius)
      case ShapeType.Rectangle(width, height) =>
        import Point2D.*

        val center = entity.position
        val angle = entity.orientation

        val halfWidth = width / 2
        val halfHeight = height / 2

        // Define the 4 corners as vectors relative to center
        val topRightRel = Point2D(halfWidth, halfHeight)
        val topLeftRel = Point2D(-halfWidth, halfHeight)
        val bottomLeftRel = Point2D(-halfWidth, -halfHeight)
        val bottomRightRel = Point2D(halfWidth, -halfHeight)

        // Rotate and then translate each corner
        def rotateAroundCenter(relative: Vector2D): Point2D =
          import Vector2D.*
          val rotated = relative.rotate(angle)
          Point2D(center.x + rotated.x, center.y + rotated.y)

        val topRight = rotateAroundCenter(topRightRel)
        val topLeft = rotateAroundCenter(topLeftRel)
        val bottomLeft = rotateAroundCenter(bottomLeftRel)
        val bottomRight = rotateAroundCenter(bottomRightRel)

        // Create lines
        val topLine = Line(topLeft, topRight)
        val bottomLine = Line(bottomLeft, bottomRight)
        val leftLine = Line(topLeft, bottomLeft)
        val rightLine = Line(topRight, bottomRight)

        val lines = Seq(topLine, bottomLine, leftLine, rightLine)
        lines.flatMap(intersectRayWithLine).minOption
    end match

  end intersectRay
end Ray
