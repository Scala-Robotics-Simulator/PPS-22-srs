package io.github.srs.model.illumination

import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }

/**
 * A light map that computes and caches light fields for a valid environment.
 *
 * @tparam F
 *   The effect type used for computations.
 */
trait LightMap[F[_]]:

  /**
   * Get (or build) precomputed static occlusion data for an environment.
   *
   * @param env
   *   The environment containing static entities and obstacles.
   * @return
   *   A prepared static data instance wrapped in the effect type `F`.
   */
  def prepared(env: Environment): F[Illumination.Prepared]

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

  /**
   * Drop cached statics so the next call recomputes them.
   *
   * @return
   *   A unit value wrapped in the effect type `F`.
   */
  def clear: F[Unit]

end LightMap

/**
 * Companion object for the `LightMap` trait, providing a factory method to create instances.
 */
object LightMap:

  import cats.effect.Ref
  import cats.effect.kernel.Sync
  import cats.syntax.all.*

  /**
   * Create a new stateful [[LightMap]] service backed by a small cache of precomputed static occlusion data.
   *
   * The cache is invalidated when static entities geometry changes.
   *
   * @param fov
   *   The Field of View (FoV) engine used for light propagation.
   * @param scale
   *   The scale factor for the grid.
   * @tparam F
   *   The effect type (e.g., IO, Task) used for computations.
   * @return
   *   A new `LightMap` instance wrapped in the effect type `F`.
   */
  def create[F[_]: Sync](fov: FovEngine, scale: ScaleFactor): F[LightMap[F]] =
    Ref.of[F, Option[Illumination.Prepared]](None).map { cache =>
      new LightMap[F]:
        /**
         * Reuses the cached static data if valid, or prepares new static data.
         *
         * @param env
         *   The environment to prepare static data for.
         * @return
         *   The prepared static data wrapped in the effect type `F`.
         */
        private def reuseOrPrepare(env: Environment): F[Illumination.Prepared] =
          cache.get.flatMap:
            case Some(prev) =>
              val next = Illumination.reuseOrRebuild(prev, env)
              if next eq prev then prev.pure[F]
              else cache.set(Some(next)).as(next)
            case None =>
              val next = Illumination.prepareStatics(env, scale)
              cache.set(Some(next)).as(next)

        /**
         * Prepares static data for the given environment.
         *
         * @param env
         *   The environment containing static entities and obstacles.
         * @return
         *   A prepared static data object wrapped in the effect type `F`.
         */
        def prepared(env: Environment): F[Illumination.Prepared] = reuseOrPrepare(env)

        /**
         * Computes the light field for the given environment.
         *
         * @param env
         *   The environment containing entities and obstacles.
         * @param includeDynamic
         *   Whether to include dynamic entities in the computation.
         * @return
         *   The computed light field wrapped in the effect type `F`.
         */
        def computeField(env: Environment, includeDynamic: Boolean): F[LightField] =
          reuseOrPrepare(env).map(p => Illumination.field(env, p, fov, includeDynamic))

        /**
         * Clears the cached static data.
         *
         * @return
         *   A unit value wrapped in the effect type `F`.
         */
        def clear: F[Unit] = cache.set(None)
    }
end LightMap
