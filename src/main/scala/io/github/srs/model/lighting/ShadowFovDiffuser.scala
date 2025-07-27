package io.github.srs.model.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.EnvironmentView
import io.github.srs.model.*
import io.github.srs.model.entity.Point2D.*

/**
 * Implements light diffusion with shadow calculation using a Field of View (FOV) engine.
 *
 * The [[Diffuser]] calculates how light spreads through the environment, taking into account obstacles that create
 * shadows and light attenuation over distance.
 *
 * @param fov
 *   The Field of View engine used for computing light visibility (defaults to [[SquidLibFov]])
 */
final class ShadowFovDiffuser(fov: FovEngine = SquidLibFov) extends Diffuser[EnvironmentView, LightState]:

  /**
   * Computes the next lighting state based on the current environment.
   *
   * @param environment
   *   The current state of the environment including lights and obstacles
   * @param current
   *   The current light state (unused in this implementation)
   * @return
   *   A new LightState representing the calculated lighting
   */
  override def step(environment: EnvironmentView)(current: LightState): LightState =
    val width = environment.width
    val height = environment.height
    val gridSize = width * height

    val emptyGrid: ArraySeq[Lux] = ArraySeq.fill(gridSize)(0.0)
    val illuminatedGrid: ArraySeq[Lux] = environment.lights.foldLeft(emptyGrid) { (accumulator, light) =>
      val lightEffect = calculateLightEffect(light, environment, width)
      accumulator.lazyZip(lightEffect).map(_ + _)
    }

    LightState.fromArray(width, illuminatedGrid)

  /**
   * Returns the light intensity at a specific cell in the current state.
   *
   * @param state
   *   The current light state
   * @param cell
   *   The cell position to query
   * @return
   *   The light intensity (Lux) at the specified cell
   */
  inline override def intensityAt(state: LightState)(cell: Cell): Lux =
    state.intensity(cell)

  /**
   * Calculates the light effect from a single light source across the environment.
   *
   * This method computes:
   *   - Light visibility using FOV engine
   *   - Distance-based attenuation
   *   - Final light intensity for each cell affected by the light
   *
   * @param light
   *   The light source
   * @param environment
   *   The current environment state
   * @param width
   *   The width of the environment grid
   * @return
   *   An ArraySeq containing the light intensities for each cell
   */
  private def calculateLightEffect(
      light: StaticEntity.Light,
      environment: EnvironmentView,
      width: Int,
  ): ArraySeq[Lux] =
    val squaredRadius = light.radius * light.radius
    val (sourceX, sourceY) = light.position
    val lightVisibility = fov.compute(environment.resistance)(
      light.position.toCell,
      light.radius.toInt,
    )

    lightVisibility.iterator.zipWithIndex.map { case (visibilityFactor, index) =>
      val targetX = index % width
      val targetY = index / width
      val squaredDistance = (sourceX - targetX) * (sourceX - targetX) +
        (sourceY - targetY) * (sourceY - targetY)
      // Calculate attenuation based on inverse square law with a custom attenuation factor
      val attenuationFactor = 1.0 / (1.0 + light.attenuation * (squaredDistance / squaredRadius))
      // Final light intensity combines visibility, source intensity, and attenuation
      visibilityFactor * light.intensity * attenuationFactor
    }.to(ArraySeq)
  end calculateLightEffect
end ShadowFovDiffuser
