package io.github.srs.utils

import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.environment.Environment

object SpatialUtils:

  def discreteCell(pos: Point2D, cellSize: Double = 1.0): (Int, Int) =
    val cellX = (pos.x / cellSize).toInt
    val cellY = (pos.y / cellSize).toInt
    (cellX, cellY)

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

  def nearbyVisitedPositions(
      pos: (Int, Int),
      m: Map[(Int, Int), Double],
      width: Int,
      height: Int,
  ): (Map[(Int, Int), Double], List[Double]) =
    val (x, y) = pos

    val m2 = m.view.mapValues(_ * 0.999).toMap
    val newM = m2 + ((x, y) -> 1.0)

    def insideMap(px: Int, py: Int): Boolean =
      px >= 0 && px < width && py >= 0 && py < height

    val values =
      for
        dx <- -2 to 2
        dy <- -2 to 2
      yield
        val px = x + dx
        val py = y + dy

        if !insideMap(px, py) then -1.0 else newM.getOrElse((px, py), 0.0)

    (newM, values.toList)
  end nearbyVisitedPositions

end SpatialUtils
