package io.github.srs.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.Cell
import squidpony.squidgrid.FOV


/**
 * Trait representing a Field of View (FOV) engine capable of computing visible light intensities in a grid-based
 * two-dimensional environment.
 *
 * The [[FovEngine]] is used to calculate the light intensities emanating from a specific origin cell within a defined
 * radius, considering factors such as obstacles and resistance values
 */
trait FovEngine:

    /**
     * Computes the field of view (FOV) for a given resistance map, origin cell, and radius.
     *
     * @param resistance
     *   A 2D array representing the resistance values at each cell in the grid. Higher values indicate more resistance to
     *   light.
     * @param origin
     *   The starting cell from which the FOV is computed.
     * @param radius
     *   The maximum distance from the origin to consider for visibility.
     * @return
     *   An immutable sequence of doubles representing the light intensity at each cell in the grid.
     */
  def compute(resistance: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double]

/**
 * An implementation of the [[FovEngine]] interface using the SquidLib FOV algorithm.
 *
 * The [[SquidLibFov]] object provides functionality to calculate field of view (FOV) for a grid-based
 * environment. It computes the intensity of light (or visibility) at different cells in the grid
 * based on a resistance map, an origin point, and a specified radius.
 */
object SquidLibFov extends FovEngine:

  override def compute(res: Array[Array[Double]])(origin: Cell, radius: Int): ArraySeq[Double] =
    val buf = Array.ofDim[Double](res.length, res.head.length)
    FOV.reuseFOV(res, buf, origin.x, origin.y, radius)
    ArraySeq.unsafeWrapArray(buf.flatten)
