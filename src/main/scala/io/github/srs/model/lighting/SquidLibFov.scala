package io.github.srs.model.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.Cell
import squidpony.squidgrid.FOV

/**
 * Implementation of the [[FovEngine]] interface using the SquidLib FOV algorithm.
 *
 * The [[SquidLibFov]] object provides functionality to calculate field of view (FOV) for a grid-based environment.
 *
 * It computes the intensity of light (or visibility) at different cells in the grid based on a resistance map, an
 * origin point, and a specified radius.
 */
object SquidLibFov extends FovEngine:

  override def compute(res: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double] =
    val buf = Array.ofDim[Double](res.length, res.head.length)
    FOV.reuseFOV(res, buf, origin.x, origin.y, radius)
    ArraySeq.unsafeWrapArray(buf.flatten)
