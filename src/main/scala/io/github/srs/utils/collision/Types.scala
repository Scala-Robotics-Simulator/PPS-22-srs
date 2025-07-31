package io.github.srs.utils.collision

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.utils.*

/**
 * Represents a 2D vector in the collision system.
 */
type Vector2D = Point2D

/**
 * Companion object for Vector2D, providing utility methods for vector operations.
 */
object Vector2D:

  /**
   * Creates a new Vector2D instance from the given x and y coordinates.
   * @param x
   *   the x-coordinate of the vector
   * @param y
   *   the y-coordinate of the vector
   * @return
   *   a new Vector2D instance representing the vector in 2D space
   */
  def apply(x: Double, y: Double): Vector2D = Point2D(x, y)

  extension (v: Vector2D)

    /**
     * Rotates the vector by the specified angle.
     * @param angle
     *   the angle to rotate the vector by
     * @return
     *   a new Vector2D instance representing the rotated vector
     */
    def rotate(angle: Orientation): Vector2D =
      import Point2D.*
      val radians = angle.toRadians
      val cos = Math.cos(radians)
      val sin = Math.sin(radians)
      Point2D(
        v.x * cos - v.y * sin,
        v.x * sin + v.y * cos,
      )

    /**
     * Projects the vector onto a line defined by its origin and direction.
     * @param line
     *   the line onto which the vector will be projected
     * @return
     *   a new Vector2D instance representing the projection of the vector onto the line
     */
    def project(line: Line): Vector2D =
      import Point2D.*
      val lineDirection = line.direction
      val dotProduct = line.direction.x * (v.x - line.origin.x) + line.direction.y * (v.y - line.origin.y)
      Vector2D(
        line.origin.x + (dotProduct * lineDirection.x),
        line.origin.y + (dotProduct * lineDirection.y),
      )
  end extension
end Vector2D

/**
 * Represents a line in the collision system, defined by an origin point and a direction vector.
 */
private[collision] type Line = (origin: Point2D, direction: Point2D)

/**
 * Companion object for Line, providing utility methods for creating and manipulating lines.
 */
private[collision] object Line:

  /**
   * Creates a new Line instance from the given origin and direction points.
   * @param origin
   *   the starting point of the line
   * @param direction
   *   the direction vector of the line
   * @return
   *   a new Line instance representing the line in 2D space
   */
  def apply(origin: Point2D, direction: Point2D): Line = (origin, direction)

/**
 * Represents an axis in the collision system, defined by two lines: one for the x-axis and one for the y-axis.
 */
private[collision] type Axis = (x: Line, y: Line)

/**
 * Companion object for Axis, providing utility methods for creating and manipulating axes.
 */
private[collision] object Axis:

  /**
   * Creates a new Axis instance from the given x and y lines.
   * @param x
   *   the line representing the x-axis
   * @param y
   *   the line representing the y-axis
   * @return
   *   a new Axis instance representing the axis in 2D space
   */
  def apply(x: Line, y: Line): Axis = (x, y)

  /**
   * Creates an axis with the origin at (0, 0) and the x-axis pointing to the right and the y-axis pointing down.
   * @return
   *   a new Axis instance representing the default axis in 2D space
   */
  private def origin: Axis =
    val origin = Point2D(0, 0)
    val xAxis = Line(origin, Point2D(1, 0))
    val yAxis = Line(origin, Point2D(0, 1))
    Axis(xAxis, yAxis)

  /**
   * Creates an axis oriented at a specific position and orientation.
   * @param position
   *   the position of the axis origin
   * @param orientation
   *   the orientation of the axis
   * @return
   */
  def oriented(position: Point2D, orientation: Orientation): Axis =
    import Vector2D.rotate
    val origin = Axis.origin
    val rotatedX = origin.x.direction.rotate(orientation)
    val rotatedY = origin.y.direction.rotate(orientation)
    Axis(
      Line(position, rotatedX),
      Line(position, rotatedY),
    )

end Axis

/**
 * Represents a rectangle in the collision system, defined by its top-left, top-right, bottom-right, and bottom-left
 * corners.
 */
private[collision] type Rectangle = (tl: Point2D, tr: Point2D, br: Point2D, bl: Point2D)

/**
 * Companion object for Rectangle, providing utility methods for creating and manipulating rectangles.
 */
private[collision] object Rectangle:

  /**
   * Creates a new Rectangle instance from the given corner points.
   * @param tl
   *   the top-left corner of the rectangle
   * @param tr
   *   the top-right corner of the rectangle
   * @param br
   *   the bottom-right corner of the rectangle
   * @param bl
   *   the bottom-left corner of the rectangle
   * @return
   *   a new Rectangle instance representing the rectangle in 2D space
   */
  def apply(tl: Point2D, tr: Point2D, br: Point2D, bl: Point2D): Rectangle = (tl, tr, br, bl)

  /**
   * Creates a rectangle from an axis and a shape.
   * @param axis
   *   the axis defining the orientation and position of the rectangle
   * @param shape
   *   the shape defining the dimensions of the rectangle
   * @return
   *   a new Rectangle instance representing the rectangle in 2D space
   */
  def fromAxisAndShape(axis: Axis, shape: ShapeType.Rectangle): Rectangle =
    import Point2D.*
    val rx = axis.x.direction * (shape.width / 2)
    val ry = axis.y.direction * (shape.height / 2)
    val tl = axis.x.origin + rx + ry
    val tr = axis.x.origin + rx - ry
    val br = axis.x.origin - rx - ry
    val bl = axis.x.origin - rx + ry
    Rectangle(tl, tr, br, bl)

  extension (rect: Rectangle)

    /**
     * Returns the corners of the rectangle in a list.
     * @return
     *   a List of Point2D representing the corners of the rectangle
     */
    def getCornersList: Seq[Point2D] = List(rect.tl, rect.tr, rect.br, rect.bl)

end Rectangle

/**
 * Utility class for rectangle collision detection. This class represents a rectangle collider defined by its position,
 * shape, and orientation.
 * @param position
 *   the position of the rectangle in 2D space
 * @param shape
 *   the shape of the rectangle, defined by its width and height
 * @param orientation
 *   the orientation of the rectangle in 2D space
 */
private[collision] final case class RectangleCollider(
    position: Point2D,
    shape: ShapeType.Rectangle,
    orientation: Orientation,
):
  /**
   * The axis of the rectangle collider, defined by its position and orientation. This axis is used to determine the
   * orientation of the rectangle in 2D space.
   * @return
   *   an Axis instance representing the oriented axis of the rectangle
   */
  private[collision] lazy val axis: Axis = Axis.oriented(position, orientation)

  /**
   * The rectangle defined by the axis and shape of the rectangle collider. This rectangle is used for collision
   * detection against other entities.
   * @return
   *   a Rectangle instance representing the rectangle collider in 2D space
   */
  private lazy val rectangle = Rectangle.fromAxisAndShape(axis, shape)
  import Rectangle.*

  /**
   * The corners of the rectangle collider, represented as a sequence of Point2D. These corners are used for collision
   * detection and spatial queries.
   * @return
   *   a Seq of Point2D representing the corners of the rectangle collider, in the order of top-left, top-right,
   *   bottom-right, and bottom-left.
   */
  private[collision] lazy val corners: Seq[Point2D] = rectangle.getCornersList

  /**
   * The center point of the rectangle.
   * @return
   *   a Point2D representing the center of the rectangle.
   */
  def center: Point2D = position

  /**
   * Returns the size of the rectangle as a Point2D.
   * @return
   *   a Point2D representing the width and height of the rectangle.
   */
  def size: Point2D = Point2D(shape.width, shape.height)
end RectangleCollider
