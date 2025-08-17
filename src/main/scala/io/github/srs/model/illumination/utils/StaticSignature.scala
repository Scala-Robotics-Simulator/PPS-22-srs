package io.github.srs.model.illumination.utils

import scala.util.hashing.MurmurHash3

import io.github.srs.model.entity.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.model.{ GridDims, ScaleFactor }

/**
 * The [[StaticSignature]] object provides methods to compute a static signature for an environment.
 *
 * The signature is a hash that uniquely identifies the static entities in the environment, allowing for efficient
 * caching and reuse of precomputed static data.
 */
object StaticSignature:

  /**
   * Computes a static signature for the given environment to decide caching and reuse.
   *
   * @param env
   *   The environment containing static entities.
   * @param scale
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @return
   *   An integer hash representing the static signature of the environment.
   */
  def of(env: Environment, scale: ScaleFactor): Int =
    val dims = GridDims.from(env)(scale)
    of(env, scale, dims)

  /**
   * Computes a static signature for the given environment to decide caching and reuse.
   *
   * @param env
   *   The environment containing static entities.
   * @param scale
   *   The scale factor used to convert world coordinates to grid cell coordinates.
   * @param dims
   *   The dimensions of the grid in cells.
   * @return
   *   An integer hash representing the static signature of the environment.
   */
  def of(env: Environment, scale: ScaleFactor, dims: GridDims): Int =
    val items =
      env.entities.collect {
        case o: StaticEntity.Obstacle =>
          ("O", o.position._1, o.position._2, o.orientation.degrees, o.width, o.height)
        case b: StaticEntity.Boundary =>
          ("B", b.position._1, b.position._2, b.orientation.degrees, b.width, b.height)
      }.toIndexedSeq.sorted
    MurmurHash3.productHash((scale, dims.widthCells, dims.heightCells, MurmurHash3.seqHash(items)))
end StaticSignature
