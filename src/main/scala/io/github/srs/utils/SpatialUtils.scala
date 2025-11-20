package io.github.srs.utils

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
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

  private def isCellFree(cellX: Int, cellY: Int, env: Environment, agentRadius: Double, cellSize: Double): Boolean =
    env.entities.forall:
      case o: Obstacle =>
        val centerX = cellX * cellSize + cellSize / 2
        val centerY = cellY * cellSize + cellSize / 2

        val closestX = math.max(o.position.x, math.min(centerX, o.position.x + o.width))
        val closestY = math.max(o.position.y, math.min(centerY, o.position.y + o.height))

        val dx = centerX - closestX
        val dy = centerY - closestY
        math.sqrt(dx * dx + dy * dy) > agentRadius

      case _ => true

  def estimateRealCoverage(
      visitedCells: Set[(Int, Int)],
      env: Environment,
      agentRadius: Double,
      cellSize: Double,
  ): Double =
    val nX = (env.width / cellSize).toInt
    val nY = (env.height / cellSize).toInt

    val freeCells = for
      x <- 0 until nX
      y <- 0 until nY
      if isCellFree(x, y, env, agentRadius, cellSize)
    yield (x, y)

    if freeCells.isEmpty then 0.0
    else visitedCells.count(freeCells.contains).toDouble / freeCells.size.toDouble

  def countExplorableCells(
      env: Environment,
      agentRadius: Double,
      cellSize: Double,
  ): Int =
    val nX = (env.width / cellSize).toInt
    val nY = (env.height / cellSize).toInt

    (for
      x <- 0 until nX
      y <- 0 until nY
      if isCellFree(x, y, env, agentRadius, cellSize)
    yield ()).size

  def explorableThreshold(
      env: Environment,
      agentRadius: Double,
      cellSize: Double,
      fraction: Double,
  ): Int =
    val free = countExplorableCells(env, agentRadius, cellSize)
    (free * fraction).toInt

end SpatialUtils
