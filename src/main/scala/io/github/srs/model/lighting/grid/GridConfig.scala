package io.github.srs.model.lighting.grid

import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.bounded

/**
 * Enumeration representing the types of cell aggregation.
 */
enum CellAggregation derives CanEqual:
  case Mean
  case Max
  case Min

/**
 * Configuration for the lighting grid.
 *
 * @param subdivisionsPerCell
 *   The number of subdivisions per cell (must be a positive integer).
 * @param aggregation
 *   The aggregation method to use for cell values.
 * @param robotsBlockLight
 *   If true, robots will block light in the grid.
 * @param defaultCellOpacity
 *   The default opacity of each cell (must be between 0.0 and 1.0).
 */
private final case class GridConfig(
    subdivisionsPerCell: Int,
    aggregation: CellAggregation,
    robotsBlockLight: Boolean,
    defaultCellOpacity: Double,
) derives CanEqual

object GridConfig:

  /**
   * Constructor for creating a [[GridConfig]] instance with [[Validation]].
   *
   * @param subdivisionsPerCell
   *   The number of subdivisions per cell (must be >= 1).
   * @param aggregation
   *   The aggregation method to use (default is `CellAggregation.Mean`).
   * @param robotsBlockLight
   *   Whether robots block light in the grid (default is `false`).
   * @param defaultCellOpacity
   *   The default opacity of each cell (must be between 0.0 and 1.0, default is `0.0`).
   * @return
   *   A `Validation` containing the validated `GridConfig` instance or validation errors.
   */
  def make(
      subdivisionsPerCell: Int,
      aggregation: CellAggregation = CellAggregation.Mean,
      robotsBlockLight: Boolean = false,
      defaultCellOpacity: Double = 0.0,
  ): Validation[GridConfig] =
    for
      subs <- bounded("subdivisionsPerCell", subdivisionsPerCell, 1, Int.MaxValue, includeMax = true)
      op <- bounded("defaultCellOpacity", defaultCellOpacity, 0.0, 1.0, includeMax = true)
    yield GridConfig(subs, aggregation, robotsBlockLight, op)

  extension (cfg: GridConfig)

    /**
     * Validates the current `GridConfig` instance.
     *
     * @return
     *   A `Validation` containing the validated `GridConfig` instance or validation errors.
     */
    def validate: Validation[GridConfig] =
      make(cfg.subdivisionsPerCell, cfg.aggregation, cfg.robotsBlockLight, cfg.defaultCellOpacity)
end GridConfig
