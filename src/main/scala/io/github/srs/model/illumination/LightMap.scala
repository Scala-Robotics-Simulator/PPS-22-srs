package io.github.srs.model.illumination

import cats.effect.Sync
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }

/**
 * Light map for computing illumination fields.
 *
 * @tparam F
 *   Effect type for computations (e.g., IO)
 */
trait LightMap[F[_]]:
  /**
   * Compute the [[io.github.srs.model.illumination.model.LightField]] for the given environment.
   *
   * @param env
   *   The [[io.github.srs.model.environment.Environment]] containing entities and lights
   * @param includeDynamic
   *   Whether to include dynamic entities in the computation
   * @return
   *   An effectful computation yielding the computed [[io.github.srs.model.illumination.model.LightField]]
   */
  def computeField(env: Environment, includeDynamic: Boolean): F[LightField]

/**
 * Companion object for creating instances of [[LightMap]].
 */
object LightMap:

  /**
   * Create a new [[LightMap]] instance.
   *
   * @param scale
   *   The scale factor for the light map
   * @param fov
   *   The field-of-view engine to use for computations
   * @tparam F
   *   Effect type for computations (e.g., IO)
   * @return
   *   An effectful computation yielding a new [[LightMap]] instance
   */
  def create[F[_]: Sync](scale: ScaleFactor, fov: FovEngine): F[LightMap[F]] =
    Sync[F].pure(new LightMapImpl(scale, fov))

  /**
   * Implementation of the [[LightMap]] trait that computes illumination fields based on a given scale factor and
   * Field-of-View (FoV) engine.
   *
   * @param scale
   *   The scale factor used to map world coordinates to grid coordinates.
   * @param fov
   *   The Field-of-View engine responsible for performing light propagation and visibility computations.
   * @tparam F
   *   The effect type supporting synchronization primitives (e.g., IO).
   */
  private class LightMapImpl[F[_]: Sync](scale: ScaleFactor, fov: FovEngine) extends LightMap[F]:

    def computeField(env: Environment, includeDynamic: Boolean): F[LightField] =
      Sync[F].delay:
        IlluminationLogic.computeLightField(scale)(fov)(includeDynamic)(env)
end LightMap
