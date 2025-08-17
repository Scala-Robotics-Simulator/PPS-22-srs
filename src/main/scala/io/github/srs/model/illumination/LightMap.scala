package io.github.srs.model.illumination

import cats.effect.Sync
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }

trait LightMap[F[_]]:
  /**
   * Compute the final light field, optionally includes dynamic occluders.
   *
   * @param env
   *   The environment containing entities and obstacles.
   * @param includeDynamic
   *   Whether to include dynamic entities in the computation.
   * @return
   *   The computed light field wrapped in the effect type `F`.
   */
  def computeField(env: Environment, includeDynamic: Boolean): F[LightField]

object LightMap:

  /**
   * Create a stateless [[LightMap]] facade that computes from the current environment
   *
   * @param fov
   *   The Field of View (FoV) engine used for light propagation.
   * @param scale
   *   The scale factor for the grid.
   * @tparam F
   *   The effect type (e.g., IO, Task) used for computations.
   * @return
   *   A new [[LightMap]] instance wrapped in the effect type `F`.
   */
  def create[F[_]](fov: FovEngine, scale: ScaleFactor)(using F: Sync[F]): F[LightMap[F]] =
    F.pure[LightMap[F]]((env: Environment, includeDynamic: Boolean) =>
      F.delay {
        val prepared = Illumination.prepareStatics(env, scale)
        Illumination.field(env, prepared, fov, includeDynamic)
      },
    )
end LightMap
