package io.github.srs.model.illumination

import scala.collection.immutable.ListMap

import cats.effect.Sync
import cats.effect.kernel.Ref
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }
import io.github.srs.model.illumination.utils.{ LightFieldSignature, StaticSignature }

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
   * Create a stateful [[LightMap]] facade that caches computed light fields and
   * the static precomputation results required to build them.
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
    F.flatMap(Ref.of[F, ListMap[Int, Illumination.Prepared]](ListMap.empty)) { statRef =>
      F.flatMap(Ref.of[F, ListMap[Int, LightField]](ListMap.empty)) { fieldRef =>

        def getPrepared(env: Environment): F[Illumination.Prepared] =
          F.flatMap(F.delay(StaticSignature.of(env, scale))) { sig =>
            F.flatMap(statRef.get) { m =>
              m.get(sig) match
                case Some(p) => F.pure(p)
                case None =>
                  val p = Illumination.prepareStatics(env, scale)
                  val updated = (m + (sig -> p)).takeRight(maxEntries)
                  F.map(statRef.set(updated))(_ => p)
            }
          }

        def getField(env: Environment, includeDynamic: Boolean): F[LightField] =
          F.flatMap(F.delay(LightFieldSignature.of(env, scale, includeDynamic))) { sig =>
            F.flatMap(fieldRef.get) { m =>
              m.get(sig) match
                case Some(fld) => F.pure(fld)
                case None =>
                  F.flatMap(getPrepared(env)) { p =>
                    val fld = Illumination.field(env, p, fov, includeDynamic)
                    val updated = (m + (sig -> fld)).takeRight(maxEntries)
                    F.map(fieldRef.set(updated))(_ => fld)
                  }
            }
          }

        F.pure[LightMap[F]] { (env: Environment, includeDynamic: Boolean) =>
          getField(env, includeDynamic)
        }
      }
    }
end LightMap
