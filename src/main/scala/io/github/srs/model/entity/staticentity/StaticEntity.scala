package io.github.srs.model.entity.staticentity

import io.github.srs.model.entity.*
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.*
import io.github.srs.utils.SimulationDefaults

/**
 * Represents a static entity in a two-dimensional space.
 *
 * A static entity is characterized by its position, orientation, and shape.
 */
enum StaticEntity(val position: Point2D, val orientation: Orientation) extends Entity:

  /**
   * The [[Obstacle]] represents a rectangular obstacle in the simulation environment.
   *
   * @param pos
   *   center position of the obstacle
   * @param orient
   *   orientation of the obstacle
   * @param width
   *   width of the obstacle
   * @param height
   *   height of the obstacle
   */
  case Obstacle(
      pos: Point2D,
      orient: Orientation,
      width: Double,
      height: Double,
  ) extends StaticEntity(pos, orient)

  /**
   * The [[Light]] represents a light source in the simulation environment.
   *
   * @param pos
   *   center position of the light
   * @param orient
   *   orientation of the light
   * @param radius
   *   radius of the light's influence
   * @param intensity
   *   intensity of the light
   * @param attenuation
   *   attenuation factor of the light
   */
  case Light(
      pos: Point2D,
      orient: Orientation,
      radius: Double = SimulationDefaults.StaticEntity.Light.radius,
      illuminationRadius: Double,
      intensity: Double,
      attenuation: Double,
  ) extends StaticEntity(pos, orient)

  /**
   * The [[StaticEntity.Boundary]] represents a rectangular boundary in the simulation environment.
   * @param pos
   *   center position of the boundary
   * @param orient
   *   orientation of the boundary
   * @param width
   *   width of the boundary
   * @param height
   *   height of the boundary
   */
  case Boundary(
      pos: Point2D,
      orient: Orientation,
      width: Double,
      height: Double,
  ) extends StaticEntity(pos, orient)

  /**
   * The shape type of the static entity.
   *
   * @return
   *   the [[ShapeType]] that defines the geometric shape of this static entity.
   */
  override def shape: ShapeType = this match
    case Obstacle(_, _, w, h) => ShapeType.Rectangle(w, h)
    case Light(_, _, r, _, _, _) => ShapeType.Circle(r)
    case Boundary(_, _, w, h) => ShapeType.Rectangle(w, h)

end StaticEntity

/**
 * Companion object for [[StaticEntity]], providing factory methods for creating instances.
 */
object StaticEntity:

  /**
   * Safely build an [[Obstacle]], reflecting the domain constraints.
   *
   * @param pos
   *   center position of the obstacle
   * @param orient
   *   orientation of the obstacle
   * @param width
   *   width of the obstacle
   * @param height
   *   height of the obstacle
   * @return
   *   [[Right]] with the created [[StaticEntity.Obstacle]] if valid, otherwise [[Left]] with a validation error.
   */
  def obstacle(
      pos: Point2D,
      orient: Orientation,
      width: Double,
      height: Double,
  ): Validation[StaticEntity] =
    for
      w <- positive("width", width)
      h <- positive("height", height)
    yield StaticEntity.Obstacle(pos, orient, w, h)

  /**
   * Safely build a [[Light]], reflecting the domain constraints.
   * @param pos
   *   center position of the light
   * @param orient
   *   orientation of the light
   * @param radius
   *   radius of the light's influence
   * @param intensity
   *   intensity of the light
   * @param attenuation
   *   attenuation factor of the light
   * @return
   *   [[Right]] with the created [[StaticEntity.Light]] if valid, otherwise [[Left]] with a validation error.
   */
  def light(
      pos: Point2D,
      orient: Orientation,
      radius: Double = SimulationDefaults.StaticEntity.Light.radius,
      illuminationRadius: Double,
      intensity: Double,
      attenuation: Double,
  ): Validation[StaticEntity] =
    for
      r <- positive("radius", radius)
      i <- positive("intensity", intensity)
      a <- positive("attenuation", attenuation)
    yield StaticEntity.Light(pos, orient, r, illuminationRadius, i, a)

  /**
   * Safely build a [[StaticEntity.Boundary]], reflecting the domain constraints.
   * @param pos
   *   center position of the boundary
   * @param orient
   *   orientation of the boundary
   * @param width
   *   width of the boundary
   * @param height
   *   height of the boundary
   * @return
   *   [[Right]] with the created [[StaticEntity.Boundary]] if valid, otherwise [[Left]] with a validation error.
   */
  def boundary(
      pos: Point2D,
      orient: Orientation,
      width: Double,
      height: Double,
  ): Validation[StaticEntity] =
    for
      w <- positiveWithZero("width", width)
      h <- positiveWithZero("height", height)
    yield StaticEntity.Boundary(pos, orient, w, h)

  object Boundary:

    /**
     * Creates boundaries for the environment based on its width and height.
     *
     * @param width
     *   the width of the environment
     * @param height
     *   the height of the environment
     * @return
     *   a Validation containing a set of boundaries if successful, otherwise an error.
     */
    def createBoundaries(width: Int, height: Int): Set[StaticEntity] =
      Set(
        // Top boundary
        StaticEntity.Boundary(
          pos = Point2D(width / 2.0, 0.0),
          orient = Orientation(0.0),
          width = width.toDouble,
          height = 0.0,
        ),
        // Bottom boundary
        StaticEntity.Boundary(
          pos = Point2D(width / 2.0, height),
          orient = Orientation(0.0),
          width = width.toDouble,
          height = 0.0,
        ),
        // Left boundary
        StaticEntity.Boundary(
          pos = Point2D(0.0, height / 2.0),
          orient = Orientation(0.0),
          width = 0.0,
          height = height.toDouble,
        ),
        // Right boundary
        StaticEntity.Boundary(
          pos = Point2D(width, height / 2.0),
          orient = Orientation(0.0),
          width = 0.0,
          height = height.toDouble,
        ),
      )
  end Boundary
end StaticEntity
