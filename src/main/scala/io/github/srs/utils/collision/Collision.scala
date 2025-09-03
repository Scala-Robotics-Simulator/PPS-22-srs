package io.github.srs.utils.collision

import io.github.srs.model.entity.ShapeType.Circle
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.utils.*
import io.github.srs.model.entity.Entity

object Collision:

  extension (e: Entity)

    /**
     * Checks if this entity collides with another entity.
     *
     * @param other
     *   the other entity to check for collision against this entity.
     * @return
     *   true if the two entities collide, false otherwise.
     */
    def collidesWith(other: Entity): Boolean =
      import Point2D.*
      (e.shape, other.shape) match
        case (ShapeType.Circle(eRadius), ShapeType.Circle(otherRadius)) =>
          e.position.distanceTo(other.position) <= (eRadius + otherRadius)
        case (ShapeType.Rectangle(w, h), ShapeType.Rectangle(_, _)) =>
          isColliding(e.position, ShapeType.Rectangle(w, h), e.orientation)(
            other.position,
            other.shape,
            other.orientation,
          )
        case (ShapeType.Circle(_), ShapeType.Rectangle(_, _)) => other.collidesWith(e)
        case (ShapeType.Rectangle(w, h), ShapeType.Circle(_)) =>
          isColliding(e.position, ShapeType.Rectangle(w, h), e.orientation)(
            other.position,
            other.shape,
            other.orientation,
          )
  end extension

  /**
   * Checks if a rectangle is colliding with another shape.
   * @param position
   *   the position of the rectangle in 2D space
   * @param shape
   *   the shape of the rectangle
   * @param orientation
   *   the orientation of the rectangle in 2D space
   * @param otherPosition
   *   the position of the other shape in 2D space
   * @param otherShape
   *   the shape of the other entity
   * @param otherOrientation
   *   the orientation of the other entity in 2D space
   * @return
   *   true if the rectangle is colliding with the other shape, false otherwise
   */
  private def isColliding(position: Point2D, shape: ShapeType.Rectangle, orientation: Orientation)(
      otherPosition: Point2D,
      otherShape: ShapeType,
      otherOrientation: Orientation,
  ): Boolean =
    otherShape match
      case ShapeType.Rectangle(otherWidth, otherHeight) =>
        val rectA = RectangleCollider(position, shape, orientation)
        val rectB = RectangleCollider(otherPosition, ShapeType.Rectangle(otherWidth, otherHeight), otherOrientation)
        isRectColliding(rectA, rectB)
      case Circle(radius) =>
        val rect = RectangleCollider(position, shape, orientation)
        isRectCircleColliding(rect, otherPosition, radius)

  /**
   * Utility method to check if two rectangles are colliding.
   * @param rectA
   *   the first rectangle collider
   * @param rectB
   *   the second rectangle collider
   * @return
   *   true if the rectangles are colliding, false otherwise
   */
  private def isRectColliding(rectA: RectangleCollider, rectB: RectangleCollider): Boolean =
    isProjectionCollide(rectA, rectB) && isProjectionCollide(rectB, rectA)

  /**
   * Checks if the projection of a rectangle collides with another rectangle.
   * @param rect
   *   the rectangle collider to check for collision
   * @param onRect
   *   the rectangle collider to check against
   * @return
   *   true if the projection of the rectangle collides with the other rectangle, false otherwise
   */
  private def isProjectionCollide(rect: RectangleCollider, onRect: RectangleCollider): Boolean =
    import Point2D.*
    val axis = onRect.axis
    val lines = List(axis.x, axis.y)
    val corners = rect.corners

    lines.zipWithIndex.forall { case (line, dimension) =>
      // Size of onRect half-size on line direction
      val rectHalfSize = if dimension == 0 then onRect.size.x / 2 else onRect.size.y / 2

      // Project corners of rect on line
      // and calculate signed distance from center of onRect.
      val projections = corners.map { corner =>
        import io.github.srs.utils.geometry2d.Vector2D.project
        val projected = corner.project(line)
        val CP = projected - onRect.center

        // Sign: Same direction of onRect axis: true.
        val sign = (CP.x * line.direction.x) + (CP.y * line.direction.y) > 0
        val signedDistance = CP.magnitude * (if sign then 1 else -1)

        signedDistance
      }

      // This will always return the correct distance, projections will never be empty
      val distance = projections.headOption.getOrElse(
        0.0,
      )

      // Find the minimum and maximum projection distances
      val (minProjection, maxProjection) = projections.foldLeft((distance, distance)):
        case ((currentMin, currentMax), distance) =>
          val newMin = if distance < currentMin then distance else currentMin
          val newMax = if distance > currentMax then distance else currentMax
          (newMin, newMax)

      // Check if the projections collide
      // Condition 1: One projection is negative and the other is positive
      val condition1 = minProjection < 0 && maxProjection > 0
      // Condition 2: Minimum projection is within the half-size of the rectangle
      val condition2 = Math.abs(minProjection) < rectHalfSize
      // Condition 3: Maximum projection is within the half-size of the rectangle
      val condition3 = Math.abs(maxProjection) < rectHalfSize

      condition1 || condition2 || condition3
    }

  end isProjectionCollide

  /**
   * Checks if a rectangle collider is colliding with a circle.
   * @param rect
   *   the rectangle collider to check for collision
   * @param circleCenter
   *   the center of the circle in 2D space
   * @param circleRadius
   *   the radius of the circle
   * @return
   *   true if the rectangle collider is colliding with the circle, false otherwise
   */
  private def isRectCircleColliding(rect: RectangleCollider, circleCenter: Point2D, circleRadius: Double): Boolean =
    import Point2D.*

    // Translate circle center into rectangle's local space
    val relative = circleCenter - rect.center
    val localX = relative.dot(rect.axis.x.direction)
    val localY = relative.dot(rect.axis.y.direction)

    val halfWidth = rect.size.x / 2
    val halfHeight = rect.size.y / 2

    // Clamp the local point to rectangle's bounds
    val closestX = Math.max(-halfWidth, Math.min(localX, halfWidth))
    val closestY = Math.max(-halfHeight, Math.min(localY, halfHeight))

    // Compute distance from closest point to circle center in local space
    val dx = localX - closestX
    val dy = localY - closestY
    val distanceSq = dx * dx + dy * dy

    distanceSq <= circleRadius * circleRadius
  end isRectCircleColliding

end Collision
