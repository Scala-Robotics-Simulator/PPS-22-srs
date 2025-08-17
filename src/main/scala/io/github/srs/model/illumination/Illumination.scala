package io.github.srs.model.illumination

import scala.collection.immutable.ArraySeq
import scala.util.hashing.MurmurHash3

import io.github.srs.model.entity.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.*
import io.github.srs.model.illumination.raster.OcclusionRaster

/**
 * The [[Illumination]] object provides methods to compute light fields in a grid-based environment. It prepares static
 * occlusion data, computes light fields from static and dynamic entities, and combines them into a single saturating
 * light field.
 */
object Illumination:

  /**
   * Immutable bundle of precomputed, static data for light-field computation.
   *
   * @param scale
   *   envirnoment→grid scale factor (cells per meter)
   * @param dims
   *   grid dimensions (in cells) derived from the current environment and scale
   * @param staticRes
   *   occlusion raster for static geometry only (obstacles + boundaries)
   * @param staticSig
   *   hash signature to detect when static precomputation can be safely reused
   */
  final case class Prepared(
      scale: ScaleFactor,
      dims: GridDims,
      staticRes: Grid[Double],
      staticSig: Int,
  )

  /**
   * Prepares the static occlusion raster for the given environment and scale.
   *
   * @param env
   *   the environment containing static entities
   * @param scale
   *   environment→grid scale factor (cells per meter)
   * @return
   *   a [[Prepared]] instance containing the static occlusion raster and its signature
   */
  def prepareStatics(env: Environment, scale: ScaleFactor): Prepared =
    given ScaleFactor = scale
    val dims = GridDims.from(env)(scale)
    val staticRes = OcclusionRaster.staticMatrix(env)
    val sig = staticSignature(env, scale, dims)
    Prepared(scale, dims, staticRes, sig)

  /**
   * Reuse an existing [[Prepared]] cache if the static environment did not change
   *
   * otherwise rebuild it.
   *
   * @param prev
   *   the previously prepared static data
   * @param env
   *   the current environment to check against the previous static signature
   * @return
   *   a [[Prepared]] instance, either reused or rebuilt
   */
  def reuseOrRebuild(prev: Prepared, env: Environment): Prepared =
    val now = staticSignature(env, prev.scale, prev.dims)
    if now == prev.staticSig then prev else prepareStatics(env, prev.scale)

  /**
   * Compute the light field for the environment using the supplied FoV engine.
   *
   * Static occlusions are taken from the [[Prepared]] cache.
   *
   * dynamic occlusions are optionally overlaid.
   *
   * @param env
   *   the environment containing entities and obstacles
   * @param prepared
   *   the precomputed static data for the environment
   * @param fov
   *   the Field of View engine used for light propagation
   * @param includeDynamic
   *   when true, dynamic entities are rasterized and overlaid on the static occlusion raster
   * @return
   *   a saturating light field in [0,1] per cell
   */
  def field(
      env: Environment,
      prepared: Prepared,
      fov: FovEngine,
      includeDynamic: Boolean,
  ): LightField =
    given ScaleFactor = prepared.scale

    val dims = prepared.dims

    // Combine static and dynamic occlusion grids if dynamic entities are included
    val resistanceGrid =
      Option
        .when(includeDynamic)(OcclusionRaster.dynamicMatrix(env))
        .fold(prepared.staticRes)(dyn => OcclusionRaster.overlay(prepared.staticRes, dyn))

    // Collect all light sources from the environment
    val lights: Vector[StaticEntity.Light] =
      env.entities.collect { case l: StaticEntity.Light => l }.toVector

    // Compute the light field for each light source
    val fields: Vector[ArraySeq[Double]] =
      lights.map { l =>
        val (sx, sy) = Cell.toCellFloor(l.position)
        val radius = Cell.radiusCells(l.illuminationRadius).toDouble
        val base = fov.compute(resistanceGrid)(sx, sy, radius)
        val k = clamp01(l.intensity)
        if k == 1.0 then base
        else if k == 0.0 then zeroField(dims)
        else base.map(_ * k)
      }

    // Combine all light fields into a single saturating light field
    val combined = combineSaturating(dims)(fields)
    LightField(dims, combined)
  end field

  /**
   * Clamp a double value to the range [0, 1].
   *
   * @param d
   *   the value to clamp
   * @return
   *   clamped value in [0, 1]
   */
  private def clamp01(d: Double): Double =
    if d.isNaN || d.isInfinite then 0.0 else math.min(1.0, math.max(0.0, d))

  /**
   * Create a zero-filled light field with the given dimensions.
   *
   * @param dims
   *   the grid dimensions
   * @return
   *   an ArraySeq filled with zeros
   */
  private def zeroField(dims: GridDims): ArraySeq[Double] =
    ArraySeq.fill(dims.widthCells * dims.heightCells)(0.0)

  /**
   * Saturating addition of two double values.
   *
   * @param a
   *   the first value
   * @param b
   *   the second value
   * @return
   *   the sum, capped at 1.0
   */
  inline private def satAdd(a: Double, b: Double): Double =
    val s = a + b
    if s >= 1.0 then 1.0 else s

  /**
   * Combine multiple light fields into a single saturating light field.
   *
   * @param dims
   *   the grid dimensions
   * @param fields
   *   the light fields to combine
   * @return
   *   a single saturating light field
   */
  private def combineSaturating(dims: GridDims)(fields: Vector[ArraySeq[Double]]): ArraySeq[Double] =
    fields.reduceOption((a, b) => a.lazyZip(b).map(satAdd)).getOrElse(zeroField(dims))

  /**
   * Compute a hash signature for the static environment to decide caching and reuse.
   *
   * @param env
   *   the environment containing static entities
   * @param scale
   *   the scale factor for the grid
   * @param dims
   *   the grid dimensions
   * @return
   *   a hash signature for the static environment
   */
  private def staticSignature(env: Environment, scale: ScaleFactor, dims: GridDims): Int =
    val items =
      env.entities.collect {
        case o: StaticEntity.Obstacle =>
          ("O", o.position._1, o.position._2, o.orientation.degrees, o.width, o.height)
        case b: StaticEntity.Boundary =>
          ("B", b.position._1, b.position._2, b.orientation.degrees, b.width, b.height)
      }.toIndexedSeq.sorted
    MurmurHash3.productHash((scale, dims.widthCells, dims.heightCells, MurmurHash3.seqHash(items)))
end Illumination
