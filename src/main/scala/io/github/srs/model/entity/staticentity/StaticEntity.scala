package io.github.srs.model.entity.staticentity

import java.util.UUID

import io.github.srs.model.entity.*
import io.github.srs.utils.SimulationDefaults
import io.github.srs.utils.SimulationDefaults.StaticEntity as Defaults

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
      id: UUID = UUID.randomUUID(),
      pos: Point2D = Defaults.Obstacle.DefaultPosition,
      orient: Orientation = Defaults.Obstacle.DefaultOrientation,
      width: Double = Defaults.Obstacle.DefaultWidth,
      height: Double = Defaults.Obstacle.DefaultHeight,
  ) extends StaticEntity(pos, orient)

  /**
   * The [[Light]] represents a light source in the simulation environment.
   *
   * @param pos
   *   center position of the light
   * @param orient
   *   orientation of the light
   * @param radius
   *   radius of the light's bulb
   * @param illuminationRadius
   *   radius of the light's illumination area
   * @param intensity
   *   intensity of the light
   * @param attenuation
   *   attenuation factor of the light
   */
  case Light(
      id: UUID = UUID.randomUUID(),
      pos: Point2D = Defaults.Light.DefaultPosition,
      orient: Orientation = Defaults.Light.DefaultOrientation,
      radius: Double = SimulationDefaults.StaticEntity.Light.DefaultRadius,
      illuminationRadius: Double = SimulationDefaults.StaticEntity.Light.DefaultIlluminationRadius,
      intensity: Double = SimulationDefaults.StaticEntity.Light.DefaultIntensity,
      attenuation: Double = SimulationDefaults.StaticEntity.Light.DefaultAttenuation,
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
      id: UUID = UUID.randomUUID(),
      pos: Point2D = Defaults.Boundary.DefaultPosition,
      orient: Orientation = Defaults.Boundary.DefaultOrientation,
      width: Double = Defaults.Boundary.DefaultWidth,
      height: Double = Defaults.Boundary.DefaultHeight,
  ) extends StaticEntity(pos, orient)

  /**
   * The shape type of the static entity.
   *
   * @return
   *   the [[ShapeType]] that defines the geometric shape of this static entity.
   */
  override def shape: ShapeType = this match
    case Obstacle(_, _, _, w, h) => ShapeType.Rectangle(w, h)
    case Light(_, _, _, r, _, _, _) => ShapeType.Circle(r)
    case Boundary(_, _, _, w, h) => ShapeType.Rectangle(w, h)

end StaticEntity

/**
 * Companion object for [[StaticEntity]], providing factory methods for creating instances.
 */
object StaticEntity:

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
      import dsl.BoundaryDsl.*
      Set(
        // Top boundary
        boundary at Point2D(width / 2.0, 0.0)
          withOrientation Orientation(0.0)
          withWidth width.toDouble
          withHeight 0.0,
        // Bottom boundary
        boundary at Point2D(width / 2.0, height)
          withOrientation Orientation(0.0)
          withWidth width.toDouble
          withHeight 0.0,
        // Left boundary
        boundary at Point2D(0.0, height / 2.0)
          withOrientation Orientation(0.0)
          withWidth 0.0
          withHeight height.toDouble,
        // Right boundary
        boundary at Point2D(width, height / 2.0)
          withOrientation Orientation(0.0)
          withWidth 0.0
          withHeight height.toDouble,
      )
    end createBoundaries
  end Boundary
end StaticEntity
