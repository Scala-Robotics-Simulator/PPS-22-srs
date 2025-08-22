package io.github.srs.model.dsl

import scala.language.postfixOps

import io.github.srs.model.Simulation.simulation
import io.github.srs.model.dsl.SimulationDSL.on
import io.github.srs.model.environment.dsl.CreationDSL.{ environment, withHeight, withWidth }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalactic.Prettifier.default

class SimulationDSLTest extends AnyFlatSpec with Matchers:

  "SimulationDSL" should "render an empty 3x3 grid" in:
    val env = environment withWidth 3 withHeight 3
    val gridString = simulation on env asGrid

    val expected =
      """+---+---+---+
        ||   |   |   |
        |+---+---+---+
        ||   |   |   |
        |+---+---+---+
        ||   |   |   |
        |+---+---+---+""".stripMargin

    gridString shouldBe expected
