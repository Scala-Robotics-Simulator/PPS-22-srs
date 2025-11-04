package io.github.srs.utils

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.environment.Environment

object SpatialUtils:

  def discreteCell(pos: Point2D, cellSize: Double): (Int, Int) =
    val cellX = (pos.x / cellSize).toInt
    val cellY = (pos.y / cellSize).toInt
    (cellX, cellY)

  /**
   * Estimates the total number of discrete cells in the environment.
   *
   * @param env
   *   the environment being explored
   * @return
   *   approximate number of total cells
   */
  private def estimateTotalCells(env: Environment, cellSize: Double): Int =
    val nX = (env.width / cellSize).toInt
    val nY = (env.height / cellSize).toInt
    nX * nY

  def estimateCoverage(
      visitedCells: collection.Set[(Int, Int)],
      env: Environment,
      cellSize: Double,
  ): Double =
    val total = estimateTotalCells(env, cellSize)
    if total == 0 then 0.0
    else visitedCells.size.toDouble / total.toDouble
end SpatialUtils
