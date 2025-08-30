package io.github.srs.model.illumination.raster

import scala.collection.immutable.BitSet

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.model.{ Grid, GridDims, ScaleFactor }
import io.github.srs.utils.SimulationDefaults.Illumination.Occlusion

/**
 * Provide occlusion rasterization for [[Entity]] shapes.
 *
 * Turns circles / rectangles into a discrete grid used for light blocking. Cells contain 0.0 (Cleared) or 1.0
 * (Occluded).
 *
 *   - Sampling at the center of each cell: (x + 0.5, y + 0.5).
 *   - Coordinate scaling: world units * [[ScaleFactor]] => grid-space (pixels/cells).
 *   - All min/max index ranges are **inclusive** and clamped to grid bounds.
 */
object OcclusionRaster:

  /**
   * Rasterize static occludes obstacles to an occlusion grid.
   *
   * @param env
   *   The [[Environment]] containing all entities
   * @param dims
   *   The dimensions of the target grid
   * @return
   *   A grid where each cell contains the occlusion value (0.0 to 1.0)
   */
  def rasterizeStatics(env: Environment, dims: GridDims)(using ScaleFactor): Grid[Double] =
    val statics = env.entities.iterator.collect:
      case ob: StaticEntity.Obstacle => ob
    rasterizeEntities(statics, dims)

  /**
   * Rasterize all dynamic entities.
   * @param env
   *   The [[Environment]] containing all entities
   * @param dims
   *   The dimensions of the target grid
   * @return
   *   A grid where each cell contains the occlusion value for dynamic entities
   */
  def rasterizeDynamics(env: Environment, dims: GridDims)(using ScaleFactor): Grid[Double] =
    val dynamics = env.entities.iterator.collect { case d: DynamicEntity => d }
    rasterizeEntities(dynamics, dims)

  /**
   * Combine two occlusion grids (overlaying), keeping the maximum opacity per cell (union of blockers).
   *
   * @param base
   *   The base occlusion grid
   * @param overlay
   *   The overlay occlusion grid to combine
   * @return
   *   A new grid with combined occlusion values
   */
  def combine(base: Grid[Double], overlay: Grid[Double]): Grid[Double] =
    Grid.overlayMax(base, overlay)

  /**
   * Rasterize a stream of entities into a set of occluded cell indices, then materialize a Grid using those indices.
   *
   * @param entities
   *   An iterator of entities to rasterize
   * @param dims
   *   The target grid dimensions
   * @param s
   *   The scale factor for coordinate conversion
   * @return
   *   A 2D array representing the occlusion grid
   */
  private def rasterizeEntities(
      entities: Iterator[Entity],
      dims: GridDims,
  )(using s: ScaleFactor): Grid[Double] =

    val occluded: BitSet = BitSet.fromSpecific(entities.flatMap { entity =>
      entity.shape match
        case ShapeType.Circle(radius) =>
          rasterizeCircle(entity.position, radius, dims)
        case ShapeType.Rectangle(width, height) =>
          rasterizeRectangle(entity.position, width, height, entity.orientation, dims)
    })

    Grid.tabulate(dims.widthCells, dims.heightCells) { (x, y) =>
      if occluded.contains(dims.toIndex(x, y)) then OpacityValue.Occluded else OpacityValue.Cleared
    }

  /**
   * Circle rasterization via scan-lines:
   *
   * For each row intersecting the circle, compute span [minX, maxX] and fill. This is cache-friendly and avoids
   * per-cell distance checks.
   *
   * @param center
   *   The circle's center position in world coordinates
   * @param radius
   *   The circle's radius in world units
   * @param dims
   *   The grid dimensions
   * @param s
   *   The scale factor for coordinate conversion
   * @return
   *   A linear indices iterator of occluded cells
   */
  private def rasterizeCircle(
      center: Point2D,
      radius: Double,
      dims: GridDims,
  )(using s: ScaleFactor): Iterator[Int] =
    // Scale center and radius to grid coordinates
    val (cx, cy) = center
    val scaledCenterX = cx * s
    val scaledCenterY = cy * s
    val scaledRadius = radius * s
    val radiusSquared = scaledRadius * scaledRadius

    //  Row range intersecting the circle (inclusive)
    val (minY, maxY) = clampSpan(scaledCenterY - scaledRadius, scaledCenterY + scaledRadius, dims.heightCells)

    for
      y <- (minY to maxY).iterator
      dy = (y.toDouble + 0.5) - scaledCenterY
      dySquared = dy * dy
      if dySquared <= radiusSquared
      dx = math.sqrt(radiusSquared - dySquared)
      left = math.ceil(scaledCenterX - dx - 0.5).toInt
      right = math.floor(scaledCenterX + dx - 0.5).toInt
      minX = math.max(0, left)
      maxX = math.min(dims.widthCells - 1, right)
      x <- (minX to maxX).iterator
    yield dims.toIndex(x, y)

  end rasterizeCircle

  /**
   * Rectangle rasterization dispatcher.
   *
   * Decides between axis-aligned and rotated rectangle algorithms based on an orientation angle.
   *
   * @param center
   *   The rectangle's center position
   * @param width
   *   The rectangle's width in world units
   * @param height
   *   The rectangle's height in world units
   * @param orientation
   *   The rectangle's orientation
   * @param dims
   *   The grid dimensions
   * @param s
   *   The scale factor for coordinate conversion
   */
  private def rasterizeRectangle(
      center: Point2D,
      width: Double,
      height: Double,
      orientation: Orientation,
      dims: GridDims,
  )(using s: ScaleFactor): Iterator[Int] =
    val deg = orientation.degrees

    if isAxisAlignedAngle(deg) then rasterizeAxisAlignedRect(center, width, height, orientation, dims)
    else rasterizeRotatedRect(center, width, height, orientation, dims)

  /**
   * Axis-aligned rectangle: fill the AABB.
   *
   * Handle axis-aligned rectangles (0, 90, 180, 270 degrees) with a fast bounding-box fill.
   *
   * @param center
   *   The rectangle's center position
   * @param width
   *   The rectangle's width
   * @param height
   *   The rectangle's height
   * @param orientation
   *   The rectangle's orientation
   * @param dims
   *   The grid dimensions
   * @param s
   *   The scale factor for coordinate conversion
   */
  private def rasterizeAxisAlignedRect(
      center: Point2D,
      width: Double,
      height: Double,
      orientation: Orientation,
      dims: GridDims,
  )(using s: ScaleFactor): Iterator[Int] =
    val degrees = orientation.degrees
    val (w, h) = if isPerpendicularAngle(degrees) then (height, width) else (width, height)

    val (cx, cy) = center
    val halfW = w / 2.0
    val halfH = h / 2.0

    val (minX, maxX) = clampSpan((cx - halfW) * s, (cx + halfW) * s, dims.widthCells)
    val (minY, maxY) = clampSpan((cy - halfH) * s, (cy + halfH) * s, dims.heightCells)

    for
      x <- (minX to maxX).iterator
      y <- (minY to maxY).iterator
    yield dims.toIndex(x, y)

  end rasterizeAxisAlignedRect

  /**
   * Rotated rectangle rasterization via inverse rotation transform:
   *
   *   1. compute tight world-space AABB analytically,
   *   2. Iterate its cells, inverse-rotate cell centers, and inside-test in local space.
   *
   * @param center
   *   The rectangle's center position
   * @param width
   *   The rectangle's width
   * @param height
   *   The rectangle's height
   * @param orientation
   *   The rectangle's orientation
   * @param dims
   *   The grid dimensions
   * @param s
   *   The scale factor for coordinate conversion
   */
  private def rasterizeRotatedRect(
      center: Point2D,
      width: Double,
      height: Double,
      orientation: Orientation,
      dims: GridDims,
  )(using s: ScaleFactor): Iterator[Int] =
    val angle = orientation.toRadians
    val cos = math.cos(angle)
    val sin = math.sin(angle)
    val halfW = width / 2.0
    val halfH = height / 2.0
    val (cx, cy) = center

    // Tight AABB of rotated rect (no corner allocation)
    val dx = math.abs(halfW * cos) + math.abs(halfH * sin)
    val dy = math.abs(halfW * sin) + math.abs(halfH * cos)

    val (gridMinX, gridMaxX) = clampSpan((cx - dx) * s, (cx + dx) * s, dims.widthCells)
    val (gridMinY, gridMaxY) = clampSpan((cy - dy) * s, (cy + dy) * s, dims.heightCells)
    val invS = 1.0 / s

    for
      x <- (gridMinX to gridMaxX).iterator
      y <- (gridMinY to gridMaxY).iterator
      // Convert a cell center back to world-space
      worldX = (x.toDouble + 0.5) * invS
      worldY = (y.toDouble + 0.5) * invS
      // Translate before rotating
      dx = worldX - cx
      dy = worldY - cy
      localX = dx * cos + worldY * sin
      localY = -dy * sin + worldY * cos
      if math.abs(localX) <= halfW && math.abs(localY) <= halfH
    yield dims.toIndex(x, y)

  end rasterizeRotatedRect

  /**
   * Check if an angle is a multiple of 90 degrees (90, 270, etc.) within a small tolerance.
   *
   * @param degrees
   *   The angle in degrees
   * @return
   *   true if the angle is a multiple of 90 degrees (within tolerance)
   */
  private def isPerpendicularAngle(degrees: Double): Boolean =
    math.abs(degrees % 180 - 90) < Occlusion.AlmostZero

  /**
   * Check if an angle is axis-aligned (0, 90, 180, 270 degrees) within a small tolerance.
   *
   * @param deg
   *   The angle in degrees
   * @return
   *   true if the angle is axis-aligned (within tolerance)
   */
  inline private def isAxisAlignedAngle(deg: Double): Boolean =
    val r = ((deg % Occlusion.FullRotation) + Occlusion.FullRotation) % Occlusion.FullRotation
    val m = r % 90.0
    math.min(m, 90.0 - m) < Occlusion.AlmostZero

  /**
   * Convert a real-valued span [min, max] (grid-space) to an inclusive, clamped integer range of cell indices for a
   * given grid size.
   * @param min
   *   The minimum coordinate (inclusive)
   * @param max
   *   The maximum coordinate (inclusive)
   * @param size
   *   The size of the grid dimension (width or height)
   * @return
   *   A tuple (lo, hi) representing the clamped index range
   */
  inline private def clampSpan(min: Double, max: Double, size: Int): (Int, Int) =
    val a = math.min(min, max)
    val b = math.max(min, max)
    val lo = math.max(0, math.floor(a).toInt)
    val hi = math.min(size - 1, math.ceil(b).toInt)
    (lo, hi)

end OcclusionRaster
