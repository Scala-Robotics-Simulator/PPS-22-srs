package io.github.srs.model.dsl

import java.util.UUID

import io.github.srs.model.Simulation
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.environment.Environment

class SimulationGridBuilder(env: Environment):

  private val width = env.width
  private val height = env.height

  private val obstacleIds: Map[UUID, Int] =
    env.entities.collect { case o: Obstacle => o.id }.zipWithIndex.toMap

  private val robotIds: Map[UUID, Int] =
    env.entities.collect { case r: Robot => r.id }.zipWithIndex.toMap

  private def cellContent(x: Int, y: Int): String =
    env.entities.collectFirst {
      case o: Obstacle
          if x >= o.pos.x && x < o.pos.x + o.width &&
            y >= o.pos.y && y < o.pos.y + o.height =>
        s"X${obstacleIds(o.id)}"

      case r: Robot if math.pow(x - r.position.x, 2) + math.pow(y - r.position.y, 2) <= math.pow(r.shape.radius, 2) =>
        val symbols = Array("→", "↘", "↓", "↙", "←", "↖", "↑", "↗")
        val idx = ((r.orientation.degrees + 22.5) / 45).toInt % 8
        s"R${robotIds(r.id)}${symbols(idx)}"
    }.getOrElse(" ")

  def asGrid: String =
    val cellW = 4
    val horizontalLine = "+" + ("-" * cellW + "+") * width
    val rows = (0 until height).map { y =>
      val rowContent = (0 until width).map { x =>
        val content = cellContent(x, y)
        val padded = content.padTo(cellW - 1, ' ')
        s"| $padded"
      }.mkString + "|"
      s"$horizontalLine\n$rowContent"
    }
    (rows :+ horizontalLine).mkString("\n")

end SimulationGridBuilder

object SimulationDSL:
  extension (sim: Simulation) infix def on(env: Environment) = new SimulationGridBuilder(env)
