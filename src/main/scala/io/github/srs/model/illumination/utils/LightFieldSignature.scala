package io.github.srs.model.illumination.utils

import scala.util.hashing.MurmurHash3

import io.github.srs.model.entity.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.model.{ GridDims, ScaleFactor }

/**
 * Computes a signature for the entire light field of an environment.
 *
 * The signature takes into account static occluders, optional dynamic entities
 * and light sources so it can be used to cache computed light fields.
 */
object LightFieldSignature:

  /**
   * Compute the signature for the given environment.
   *
   * @param env  the environment to inspect
   * @param scale the scale factor used to convert world coordinates to cells
   * @param includeDynamic whether dynamic entities should be considered
   * @return an integer hash representing the current light field state
   */
  def of(env: Environment, scale: ScaleFactor, includeDynamic: Boolean): Int =
    val dims = GridDims.from(env)(scale)
    of(env, scale, dims, includeDynamic)

  /**
   * Compute the signature for the given environment using precomputed dimensions.
   *
   * @param env  the environment to inspect
   * @param scale the scale factor used to convert world coordinates to cells
   * @param dims the grid dimensions
   * @param includeDynamic whether dynamic entities should be considered
   * @return an integer hash representing the current light field state
   */
  def of(env: Environment, scale: ScaleFactor, dims: GridDims, includeDynamic: Boolean): Int =
    val staticSig = StaticSignature.of(env, scale, dims)
    val lights = env.entities.collect {
      case l: StaticEntity.Light =>
        s"L${l.position._1},${l.position._2},${l.orientation.degrees},${l.radius},${l.illuminationRadius},${l.intensity},${l.attenuation}"
    }.toIndexedSeq.sorted
    val dyn: IndexedSeq[String] =
      if !includeDynamic then IndexedSeq.empty[String]
      else
        env.entities.collect {
          case d: DynamicEntity =>
            val shape = d.shape match
              case ShapeType.Circle(r) => s"C$r"
              case ShapeType.Rectangle(w, h) => s"R$w,$h"
            s"${d.position._1},${d.position._2},${d.orientation.degrees},$shape"
        }.toIndexedSeq.sorted
    MurmurHash3.productHash(
      (staticSig, MurmurHash3.seqHash(lights), MurmurHash3.seqHash(dyn)),
    )
end LightFieldSignature
