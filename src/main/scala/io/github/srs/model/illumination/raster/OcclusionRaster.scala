package io.github.srs.model.illumination.raster

import scala.collection.immutable.BitSet

import io.github.srs.model.entity.*
import io.github.srs.model.entity.ShapeType.*
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.model.{ Grid, GridDims, ScaleFactor }

/**
 * Object containing methods for generating occlusion rasters in a grid-based environment.
 *
 * A cell is marked occluded (1.0) iff all four of its corners lie inside (or on) * an entity’s shape, otherwise it is
 * transparent (0.0).
 */
object OcclusionRaster:

  /**
   * Rasterize static occluders (obstacles + boundaries) to a resistance grid.
   *
   * @param env
   *   The environment containing static entities.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   */
  def staticMatrix(env: Environment)(using s: ScaleFactor): Grid[Double] =
    val dims = GridDims.from(env)(s)
    toMatrix(staticBitset(env, dims), dims)

  /**
   * Rasterize dynamic occluders (all dynamic entities) to a resistance grid.
   *
   * @param env
   *   The environment containing dynamic entities.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A 2D array of doubles where 1.0 represents an occluded cell and 0.0 represents a free cell.
   */
  def dynamicMatrix(env: Environment)(using s: ScaleFactor): Grid[Double] =
    val dims = GridDims.from(env)(s)
    toMatrix(dynamicBitset(env, dims), dims)

  /**
   * Rasterize both static and dynamic occluders and combine them.
   *
   * @param env
   *   The environment containing both static and dynamic entities.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A 2D array of doubles where 1.0 represents an occluded cell and 0.0 represents a free cell.
   */
  def combinedMatrix(env: Environment)(using s: ScaleFactor): Grid[Double] =
    val dims = GridDims.from(env)(s)
    val bs = staticBitset(env, dims) | dynamicBitset(env, dims)
    toMatrix(bs, dims)

  /**
   * Overlays two matrices by taking the maximum value from each corresponding cell.
   *
   * @param base
   *   The base matrix.
   * @param over
   *   The overlay matrix.
   * @return
   *   A new matrix where each cell contains the maximum value from the corresponding cells of the base and overlay
   *   matrices.
   */
  def overlay(base: Grid[Double], over: Grid[Double]): Grid[Double] =
    Grid.overlayMax(base, over)

  // ---------- Internals: combinatori FP su BitSet ---------------------------

  /**
   * Cells fully covered by static entities (obstacles + boundaries) in the environment.
   *
   * @param env
   *   The environment containing static entities.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A BitSet where each bit represents whether a cell is occluded.
   */
  private def staticBitset(env: Environment, dims: GridDims)(using s: ScaleFactor): BitSet =
    val stat: Iterator[Entity] =
      env.entities.iterator.collect:
        case o: StaticEntity.Obstacle => o
        case b: StaticEntity.Boundary => b
    coveredCells(stat, dims)

  /**
   * Cells fully covered by dynamic entities in the environment.
   *
   * @param env
   *   The environment containing dynamic entities.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A BitSet where each bit represents whether a cell is occluded.
   */
  private def dynamicBitset(env: Environment, dims: GridDims)(using s: ScaleFactor): BitSet =
    val dyn: Iterator[Entity] = env.entities.iterator.collect { case d: DynamicEntity => d }
    coveredCells(dyn, dims)

  /**
   * Determine all cells that are completely covered by each entity's shape.
   *
   * @param entities
   *   An iterator of entities to process.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A BitSet where each bit represents whether a cell is fully covered by any entity.
   */
  private def coveredCells(entities: Iterator[Entity], dims: GridDims)(using s: ScaleFactor): BitSet =
    val idx = Indexing(dims.widthCells)
    val cells: Iterator[Int] =
      entities.flatMap(e => cellsFullyCoveredBy(e, dims, idx))
    BitSet.fromSpecific(cells)

  /**
   * Enumerate cells whose four corners lie inside (or on) an entity's shape.
   *
   * @param e
   *   The entity to process.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param idx
   *   The indexing helper for converting coordinates to indices.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   An iterator of cell indices fully covered by the entity.
   */
  private def cellsFullyCoveredBy(e: Entity, dims: GridDims, idx: Indexing)(using s: ScaleFactor): Iterator[Int] =
    val bounds = worldAabbCells(e, dims)
    if bounds.x0 > bounds.x1 || bounds.y0 > bounds.y1 then Iterator.empty
    else
      for
        y <- Iterator.range(bounds.y0, bounds.y1 + 1)
        x <- Iterator.range(bounds.x0, bounds.x1 + 1)
        if cellIsFullyCoveredBy(e, x, y)
      yield idx.toIndex(x, y)

  // ---------- Geometry: cells completely covered by a single entity -----------

  /**
   * Checks if a cell is fully covered by an entity's shape.
   *
   * @param e
   *   The entity to check.
   * @param cellX
   *   The x-coordinate of the cell.
   * @param cellY
   *   The y-coordinate of the cell.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   `true` if the cell is fully covered, `false` otherwise.
   */
  private def cellIsFullyCoveredBy(e: Entity, cellX: Int, cellY: Int)(using s: ScaleFactor): Boolean =
    val corners = cellCornersWorld(cellX, cellY)
    val tol = 1e-12 // stabilize floating-point comparisons

    e.shape match
      case ShapeType.Circle(r) =>
        if r <= 0.0 then false
        else
          val (cx, cy) = e.position
          val r2 = r * r
          corners.forall { case (x, y) => sq(x - cx) + sq(y - cy) <= r2 + tol }

      case ShapeType.Rectangle(w, h) =>
        if w <= 0.0 || h <= 0.0 then false
        else
          val (cx, cy) = e.position
          val halfW = w / 2.0
          val halfH = h / 2.0
          val theta = -e.orientation.toRadians // world→local
          val cosT = math.cos(theta)
          val sinT = math.sin(theta)

          inline def toLocal(wx: Double, wy: Double): (Double, Double) =
            val dx = wx - cx
            val dy = wy - cy
            (dx * cosT - dy * sinT, dx * sinT + dy * cosT)

          inline def insideOrOn(lx: Double, ly: Double): Boolean =
            math.abs(lx) <= halfW + tol && math.abs(ly) <= halfH + tol

          corners.iterator.map(toLocal.tupled).forall(insideOrOn.tupled)
    end match
  end cellIsFullyCoveredBy

  // ---------- Helpers: AABB in cell space + world-space cell corners ----------

  /**
   * Axis-aligned bounding box in cell coordinates that encloses the entity's shape.
   *
   * This method computes the axis-aligned bounding box (AABB) of an entity's shape in cell coordinates.
   *
   * @param e
   *   The entity to process.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A [[CellRect]] representing the AABB in cell coordinates.
   */
  private def worldAabbCells(e: Entity, dims: GridDims)(using s: ScaleFactor): CellRect =
    e.shape match
      case Circle(r) =>
        val (cx, cy) = e.position
        toCellRect(cx - r, cy - r, cx + r, cy + r, dims)

      case Rectangle(w, h) =>
        val (cx, cy) = e.position
        val halfW = w / 2.0
        val halfH = h / 2.0

        def rot(wx: Double, wy: Double): (Double, Double) =
          val dx = wx - cx
          val dy = wy - cy
          val c = math.cos(e.orientation.toRadians)
          val s = math.sin(e.orientation.toRadians)
          (cx + dx * c - dy * s, cy + dx * s + dy * c)

        val vertices = Array(
          rot(cx + halfW, cy + halfH),
          rot(cx + halfW, cy - halfH),
          rot(cx - halfW, cy + halfH),
          rot(cx - halfW, cy - halfH),
        )

        val (minX, minY, maxX, maxY) =
          vertices.foldLeft(
            (Double.PositiveInfinity, Double.PositiveInfinity, Double.NegativeInfinity, Double.NegativeInfinity),
          ) { case ((mnX, mnY, mxX, mxY), (x, y)) =>
            (math.min(mnX, x), math.min(mnY, y), math.max(mxX, x), math.max(mxY, y))
          }

        toCellRect(minX, minY, maxX, maxY, dims)

  /**
   * Convert a world-space rectangle to a clamped cell-space rectangle
   *
   * @param minX
   *   The minimum x-coordinate in world units.
   * @param minY
   *   The minimum y-coordinate in world units.
   * @param maxX
   *   The maximum x-coordinate in world units.
   * @param maxY
   *   The maximum y-coordinate in world units.
   * @param dims
   *   The dimensions of the grid in cells.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A [[CellRect]] representing the rectangle in cell coordinates.
   */
  private def toCellRect(minX: Double, minY: Double, maxX: Double, maxY: Double, dims: GridDims)(using
      s: ScaleFactor,
  ): CellRect =
    val k = s.toDouble
    val x0 = math.max(0, math.floor(minX * k).toInt)
    val y0 = math.max(0, math.floor(minY * k).toInt)
    val x1 = math.min(dims.widthCells - 1, math.ceil(maxX * k).toInt - 1)
    val y1 = math.min(dims.heightCells - 1, math.ceil(maxY * k).toInt - 1)
    CellRect(x0, y0, x1, y1)

  /**
   * Computes world-space coordinates of the four corners of a cell
   *
   * @param cellX
   *   The x-coordinate of the cell.
   * @param cellY
   *   The y-coordinate of the cell.
   * @param s
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   A list of tuples representing the world coordinates of the cell corners.
   */
  private def cellCornersWorld(cellX: Int, cellY: Int)(using s: ScaleFactor): List[(Double, Double)] =
    val k = s.toDouble
    val x0 = cellX / k
    val x1 = (cellX + 1) / k
    val y0 = cellY / k
    val y1 = (cellY + 1) / k
    List((x0, y0), (x1, y0), (x1, y1), (x0, y1))

  // ---------- Projection: BitSet → resistance matrix -------------------------

  /**
   * Convert a set of occluded cell indices into a resistance matrix in [0,1]
   *
   * @param occluded
   *   The BitSet representing occluded cells.
   * @param dims
   *   The dimensions of the grid in cells.
   * @return
   *   A 2D array of doubles where 1.0 represents an occluded cell and 0.0 represents a free cell.
   */
  private def toMatrix(occluded: BitSet, dims: GridDims): Grid[Double] =
    val width = dims.widthCells
    val height = dims.heightCells
    Grid.tabulate(width, height) { (x, y) =>
      if occluded.contains(y * width + x) then 1.0 else 0.0
    }

  /**
   * Represents a rectangle in cell coordinates.
   *
   * @param x0
   *   The minimum x-coordinate of the rectangle.
   * @param y0
   *   The minimum y-coordinate of the rectangle.
   * @param x1
   *   The maximum x-coordinate of the rectangle.
   * @param y1
   *   The maximum y-coordinate of the rectangle.
   */
  private final case class CellRect(x0: Int, y0: Int, x1: Int, y1: Int)

  /**
   * Helper class for indexing grid cells.
   *
   * @param width
   *   The width of the grid in cells.
   */
  private final case class Indexing(width: Int):
    /**
     * Converts 2D cell coordinates to a 1D index.
     *
     * @param x
     *   The x-coordinate of the cell.
     * @param y
     *   The y-coordinate of the cell.
     * @return
     *   The 1D index of the cell.
     */
    @inline def toIndex(x: Int, y: Int): Int = y * width + x

  /**
   * Squares a double value.
   *
   * @param d
   *   The value to square.
   * @return
   *   The squared value.
   */
  @inline private def sq(d: Double): Double = d * d
end OcclusionRaster
