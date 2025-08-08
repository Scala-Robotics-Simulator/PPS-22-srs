package io.github.srs.model.lighting.grid

import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.bounded

enum CellAggregation derives CanEqual:
  case Mean
  case Max
  case Min

private final case class GridConfig(
    subdivisionsPerCell: Int,
    aggregation: CellAggregation,
    robotsBlockLight: Boolean,
    defaultCellOpacity: Double,
) derives CanEqual

object GridConfig:

  /** Smart constructor validated with project `Validation` */
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

    def validate: Validation[GridConfig] =
      make(cfg.subdivisionsPerCell, cfg.aggregation, cfg.robotsBlockLight, cfg.defaultCellOpacity)
