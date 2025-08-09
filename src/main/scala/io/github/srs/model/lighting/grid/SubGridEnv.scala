package io.github.srs.model.lighting.grid

import io.github.srs.model.entity.Point2D
import io.github.srs.model.environment.Environment
import io.github.srs.model.lighting.grid.SubGridIndexing.{ coordinatesToSubCell, toSubGridSize }

/**
 * Provides methods to convert [[Environment]] coordinates to sub-grid sub-cell indices.
 */
object SubGridEnv:

  /**
   * Converts the [[Environment]] coordinates to sub-grid sub-cell indices.
   *
   * @param env
   *   The environment containing the grid dimensions.
   * @param cfg
   *   The grid configuration, including subdivisions per cell.
   * @param x
   *   The x-coordinate in the environment (coarse units).
   * @param y
   *   The y-coordinate in the environment (coarse units).
   * @return
   *   A [[SubCell]] representing the sub-grid indices corresponding to the given environment coordinates.
   */
  def toSubCell(env: Environment, cfg: GridConfig)(x: Double, y: Double): SubCell =
    coordinatesToSubCell(x, y, toSubGridSize(env.width, env.height, cfg.subdivisionsPerCell), cfg.subdivisionsPerCell)

  /**
   * Converts a [[Point2D]] to sub-grid sub-cell indices.
   *
   * @param env
   *   The environment containing the grid dimensions.
   * @param cfg
   *   The grid configuration, including subdivisions per cell.
   * @param p
   *   The point in the environment (coarse units).
   * @return
   *   A [[SubCell]] representing the sub-grid indices corresponding to the given point.
   */

  def toSubCellWithPoint(env: Environment, cfg: GridConfig)(p: Point2D): SubCell =
    import io.github.srs.model.entity.Point2D.*
    toSubCell(env, cfg)(p.x, p.y)

end SubGridEnv
