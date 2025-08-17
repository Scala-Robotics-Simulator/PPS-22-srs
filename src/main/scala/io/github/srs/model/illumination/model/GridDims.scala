package io.github.srs.model.illumination.model

import io.github.srs.model.environment.Environment

/**
 * Represents the dimensions of a grid in terms of the number of cells.
 *
 * @param widthCells
 *   The number of cells along the width of the grid.
 * @param heightCells
 *   The number of cells along the height of the grid.
 */
final case class GridDims(widthCells: Int, heightCells: Int) derives CanEqual

/**
 * Companion object providing utility methods for creating [[GridDims]] instances.
 */
object GridDims:

  /**
   * Derive grid dimensions from an [[Environment]] using a given scale.
   *
   * @param env
   *   The environment containing the dimensions in world units.
   * @param scale
   *   The scale factor used to convert world units to cell units.
   * @return
   *   A [[GridDims]] instance representing the dimensions of the grid in cells.
   */
  def from(env: Environment)(scale: ScaleFactor): GridDims =

    GridDims(
      widthCells = math.max(0, env.width) * scale,
      heightCells = math.max(0, env.height) * scale,
    )
