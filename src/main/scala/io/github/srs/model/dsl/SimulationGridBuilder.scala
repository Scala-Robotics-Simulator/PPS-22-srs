package io.github.srs.model.dsl

import io.github.srs.model.Simulation
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.environment.Environment

class SimulationGridBuilder(env: Environment):

  private val width = env.width
  private val height = env.height

  private def cellContent(x: Int, y: Int): String =
    env.entities.collectFirst {
      case o: Obstacle
          if x >= o.pos.x && x < o.pos.x + o.width &&
            y >= o.pos.y && y < o.pos.y + o.height =>
        "X"
    }.getOrElse(" ")

  def asGrid: String =
    val horizontalLine = "+---" * width + "+"
    val rows = (0 until height).map { y =>
      val rowContent = (0 until width).map { x =>
        s"| ${cellContent(x, y)} "
      }.mkString + "|"
      s"$horizontalLine\n$rowContent"
    }
    (rows :+ horizontalLine).mkString("\n")

end SimulationGridBuilder

object SimulationDSL:
  extension (sim: Simulation) infix def on(env: Environment) = new SimulationGridBuilder(env)
