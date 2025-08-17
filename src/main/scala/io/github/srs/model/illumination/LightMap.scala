package io.github.srs.model.illumination

import scala.collection.immutable.ListMap

import cats.effect.Sync
import cats.effect.kernel.Ref
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }
import io.github.srs.model.illumination.utils.StaticSignature

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

  /**
   * Create a stateful [[LightMap]] facade that caches static precomputation results
   *
   * @param fov
   *   The Field of View (FoV) engine used for light propagation.
   * @param scale
   *   The scale factor for the grid.
   * @param maxEntries
   *   Maximum number of cached entries to keep (default is 256).
   * @tparam F
   *   The effect type (e.g., IO, Task) used for computations.
   * @return
   *   A new [[LightMap]] instance wrapped in the effect type `F`.
   */
  def cached[F[_]](fov: FovEngine, scale: ScaleFactor, maxEntries: Int = 256)(using F: Sync[F]): F[LightMap[F]] =
    F.flatMap(Ref.of[F, ListMap[Int, Illumination.Prepared]](ListMap.empty)) { ref =>

      def getOrBuild(env: Environment): F[Illumination.Prepared] =
        F.flatMap(F.delay(StaticSignature.of(env, scale))) { sig =>
          F.flatMap(ref.get) { m =>
            m.get(sig) match
              case Some(p) => F.pure(p)
              case None =>
                val p = Illumination.prepareStatics(env, scale)
                val updated = (m + (sig -> p)).takeRight(maxEntries)
                F.map(ref.set(updated))(_ => p)
          }
        }

      F.pure[LightMap[F]] { (env: Environment, includeDynamic: Boolean) =>
        F.flatMap(getOrBuild(env)) { p =>
          F.delay(Illumination.field(env, p, fov, includeDynamic))
        }
      }
    }
end LightMap
