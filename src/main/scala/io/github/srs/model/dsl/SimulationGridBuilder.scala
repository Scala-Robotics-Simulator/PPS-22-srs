package io.github.srs.model.dsl

import io.github.srs.model.Simulation
import io.github.srs.model.environment.Environment

class SimulationGridBuilder(env: Environment):

  private val width = env.width
  private val height = env.height

  def asGrid: String =
    val horizontalLine = "+---" * width + "+"
    val rows = (0 until height).map { _ =>
      val emptyRow = "|   " * width + "|"
      s"$horizontalLine\n$emptyRow"
    }
    (rows :+ horizontalLine).mkString("\n")

object SimulationDSL:
  extension (sim: Simulation) infix def on(env: Environment) = new SimulationGridBuilder(env)
