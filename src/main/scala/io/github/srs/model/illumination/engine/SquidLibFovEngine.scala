package io.github.srs.model.illumination.engine

import scala.collection.immutable.ArraySeq

import io.github.srs.model.illumination.model.Grid
import io.github.srs.model.illumination.model.Grid.*
import squidpony.squidgrid.FOV

/**
 * A Field of View (FoV) engine implementation based on the SquidLib library.
 *
 * This object provides a concrete implementation of the [[FovEngine]] using SquidLib's FOV algorithms for light
 * propagation calculations in grid-based environments.
 */
object SquidLibFovEngine extends FovEngine:

  /**
   * SquidLib-based FoV implementation.
   *
   * Delegates to [[FOV.reuseFOV]] using the provided occlusion grid, writing into a reusable buffer, and finally
   * flattens the matrix in row-major order.
   *
   * @param occlusionGrid
   *   A grid of occlusion coefficients in the range [0,1], where:
   *   - 0 represents an empty cell (no occlusion to light).
   *   - 1 represents a fully blocking cell (complete resistance/occlusion to light).
   * @param startX
   *   The x-coordinate of the light source in cell space.
   * @param startY
   *   The y-coordinate of the light source in cell space.
   * @param radius
   *   The maximum distance (in cells) to propagate the light.
   * @return
   *   A row-major (x-fast) flattened ArraySeq of doubles in the range [0,1], representing the light intensity at each
   *   cell. The size equals width × height of the grid.
   */
  override def compute(
      occlusionGrid: Grid[Double],
  )(startX: Int, startY: Int, radius: Double): ArraySeq[Double] =

    val w = occlusionGrid.width
    val h = occlusionGrid.height
    if w == 0 || h == 0 then ArraySeq.empty
    else
      val buffer = Array.ofDim[Double](w, h)
      FOV.reuseFOV(occlusionGrid, buffer, startX, startY, radius)
      buffer.flattenRowMajor
end SquidLibFovEngine
