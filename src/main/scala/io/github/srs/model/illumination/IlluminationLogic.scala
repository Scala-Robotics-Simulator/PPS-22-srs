package io.github.srs.model.illumination

import scala.collection.immutable.ArraySeq

import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.lights
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.*
import io.github.srs.model.illumination.raster.OcclusionRaster
import io.github.srs.utils.SimulationDefaults.Illumination

/**
 * Illumination and light field computation engine for grid-based environments.
 *
 * The computational pipeline is as follows:
 *   - Build an occlusion grid from static and (optionally) dynamic entities.
 *   - For each light source, compute its individual contribution using a Field-of-View (FOV) engine.
 *   - Combine all individual light contributions into a final [[LightField]] by summing them with saturation (capping
 *     at 1.0).
 */
object IlluminationLogic:

  /** A type alias for a flattened, row-major grid of light intensity values. */
  private type Field = ArraySeq[Double]

  /** Saturating addition: `a + b`, capped at 1.0. This operation is associative and commutative. */
  inline private def saturatingAdd(a: Double, b: Double): Double =
    math.min(a + b, 1.0)

  /** Zips two fields together, applying saturating addition element-wise. */
  inline private def zipSat(a: Field, b: Field): Field =
    a.lazyZip(b).map(saturatingAdd)

  /** Clamps a value to the `[0.0, 1.0]` range, treating NaN and Infinity as 0.0. */
  inline private def clampTo01(value: Double): Double =
    value match
      case v if v.isNaN || v.isInfinite => 0.0
      case v if v <= 0.0 => 0.0
      case v if v >= 1.0 => 1.0
      case v => v

  /**
   * Computes the complete light field for a given environment.
   *
   * @param scale
   *   The scale factor determining the grid's resolution.
   * @param fov
   *   The Field-of-View engine used for light propagation simulation.
   * @param includeDynamic
   *   A boolean flag to determine whether dynamic entities should be included in the occlusion map.
   * @param env
   *   The environment containing lights and entities.
   * @return
   *   A [[LightField]] representing the final intensity value for each cell in the grid.
   */
  def computeLightField(scale: ScaleFactor)(fov: FovEngine)(includeDynamic: Boolean)(env: Environment): LightField =
    given ScaleFactor = scale

    val dims = GridDims.from(env)(scale)
    val lights = env.lights.toVector
    val occlusion = computeOcclusion(env, dims, includeDynamic)

    computeField(dims, lights, occlusion, fov)

  /**
   * Computes the occlusion grid from the environment.
   *
   * Occlusion values range from `0.0` (completely transparent) to `1.0` (fully opaque).
   *
   * @param env
   *   The environment containing the entities that cast shadows.
   * @param dims
   *   The dimensions of the grid to rasterize onto.
   * @param includeDynamic
   *   If true, dynamic entities are included in the occlusion map.
   * @param scale
   *   The implicit scale factor for coordinate conversion.
   * @return
   *   A `Grid[Double]` representing the occlusion map.
   */
  private def computeOcclusion(
      env: Environment,
      dims: GridDims,
      includeDynamic: Boolean,
  )(using scale: ScaleFactor): Grid[Double] =
    val staticOcclusion = OcclusionRaster.rasterizeStatics(env, dims)

    if includeDynamic then
      val dynamicOcclusion = OcclusionRaster.rasterizeDynamics(env, dims)
      OcclusionRaster.combine(staticOcclusion, dynamicOcclusion)
    else staticOcclusion

  /**
   * Computes the total light field from a pre-calculated occlusion map and a list of lights.
   *
   * This function decides whether to parallelize the computation based on grid size and light count.
   *
   * @param dims
   *   The dimensions of the light field grid.
   * @param lights
   *   A vector of light sources to compute.
   * @param occlusion
   *   The pre-computed occlusion map.
   * @param fov
   *   The FOV engine for light propagation.
   * @param scale
   *   The implicit scale factor.
   * @return
   *   The final, combined `LightField`.
   */
  private def computeField(
      dims: GridDims,
      lights: Vector[StaticEntity.Light],
      occlusion: Grid[Double],
      fov: FovEngine,
  )(using scale: ScaleFactor): LightField =
    val shouldParallelize =
      dims.totalCells >= Illumination.GridThreshold ||
        lights.sizeIs >= Illumination.LightThreshold

    val perLightFields: Vector[Field] =
      computeAllLightContributions(lights, dims, occlusion, fov, shouldParallelize)

    val combinedField: Field = perLightFields match
      case Vector() => ArraySeq.fill(dims.totalCells)(0.0)
      case Vector(single) => single
      case many =>
        val doParallelReduce = !shouldParallelize && many.sizeIs >= Illumination.LightThreshold
        if doParallelReduce then combineParallel(many)
        else combineSequential(dims.totalCells, many)

    LightField(dims, combinedField)
  end computeField

  /**
   * Computes the light contribution fields for all lights, potentially in parallel.
   *
   * @return
   *   A `Vector` where each element is a [[Field]] representing one light's contribution.
   */
  private def computeAllLightContributions(
      lights: Vector[StaticEntity.Light],
      dims: GridDims,
      occlusion: Grid[Double],
      fov: FovEngine,
      parallel: Boolean,
  )(using scale: ScaleFactor): Vector[Field] =
    lights match
      case Vector() => Vector.empty[Field]
      case _ if !parallel || lights.sizeIs < 2 =>
        lights.map(light => computeSingleLightContribution(dims, occlusion, fov, light))
      case _ =>
        import scala.collection.parallel.CollectionConverters.*
        lights.par.map(light => computeSingleLightContribution(dims, occlusion, fov, light)).seq

  /**
   * Computes the light contribution from a single light source.
   *
   * @return
   *   A [[Field]] of light intensities in the range `[0.0, 1.0]`.
   */
  private def computeSingleLightContribution(
      dims: GridDims,
      occlusion: Grid[Double],
      fov: FovEngine,
      light: StaticEntity.Light,
  )(using scale: ScaleFactor): Field =
    val (cellX, cellY) = Cell.toCellFloor(light.position)
    val radiusInCells = Cell.radiusCells(light.illuminationRadius).toDouble
    val intensityClamped = clampTo01(light.intensity)

    if !dims.inBounds(cellX, cellY) || intensityClamped == 0.0 || radiusInCells <= 0.0 then
      ArraySeq.fill(dims.totalCells)(0.0)
    else
      val rawField = fov.compute(occlusion)(cellX, cellY, radiusInCells)
      applyIntensity(intensityClamped, rawField)

  /**
   * Scales a raw light field by a given intensity factor.
   *
   * @param intensity
   *   The intensity factor, clamped to `[0.0, 1.0]`.
   * @param field
   *   The raw field from the FOV engine.
   * @return
   *   A new `Field` with the intensity applied.
   */
  private def applyIntensity(intensity: Double, field: Field): Field =
    intensity match
      case 0.0 => ArraySeq.fill(field.length)(0.0)
      case 1.0 => field
      case i => field.map(v => v * i)

  /** Sequentially combines multiple fields using saturating addition. */
  private def combineSequential(size: Int, fields: Vector[Field]): Field =
    fields.reduceOption(zipSat).getOrElse(ArraySeq.fill(size)(0.0))

  /** Combines multiple fields in parallel using saturating addition. */
  private def combineParallel(fields: Vector[Field]): Field =
    import scala.collection.parallel.CollectionConverters.*
    fields.par.reduce(zipSat)

end IlluminationLogic
