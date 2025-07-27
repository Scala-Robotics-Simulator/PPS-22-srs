package io.github.srs.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.Cell
import squidpony.squidgrid.FOV

/** Service that knows how to compute visibility. */
trait FovEngine:

  /** Curried layout → you can `compute(res)(origin, radius)` in tests. */
  def compute(resistance: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double]

/** Squidlib interpreter. */
object SquidLibFov extends FovEngine:

  override def compute(res: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double] =
    val buf = Array.ofDim[Double](res.length, res.head.length)
    FOV.reuseFOV(res, buf, origin.x, origin.y, radius)
    ArraySeq.unsafeWrapArray(buf.flatten)
