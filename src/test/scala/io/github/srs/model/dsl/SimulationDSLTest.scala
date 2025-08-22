package io.github.srs.model.dsl

import scala.language.postfixOps

import io.github.srs.model.Simulation.simulation
import io.github.srs.model.dsl.SimulationDSL.on
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalactic.Prettifier.default

class SimulationDSLTest extends AnyFlatSpec with Matchers:

  val env: Environment = environment withWidth 3 withHeight 3

  "SimulationDSL" should "render an empty 3x3 grid" in:
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

  it should "render an environment 3x3 with an obstacle" in:
    val updatedEnv = env containing (obstacle at (1, 1))
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+---+---+---+
        ||   |   |   |
        |+---+---+---+
        ||   | X |   |
        |+---+---+---+
        ||   |   |   |
        |+---+---+---+""".stripMargin

    gridString shouldBe expected

  it should "render an environment 3x3 with an obstacles 2x3" in:
    val updatedEnv = env containing (obstacle at (0, 0) withWidth 2 withHeight 2)
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+---+---+---+
        || X | X |   |
        |+---+---+---+
        || X | X |   |
        |+---+---+---+
        ||   |   |   |
        |+---+---+---+""".stripMargin
    gridString shouldBe expected

  it should "render an environment 3x3 with multiple obstacles" in:
    val updatedEnv = env containing (obstacle at (0, 0) withWidth 2 withHeight 2) and (obstacle at (2, 2))
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+---+---+---+
        || X | X |   |
        |+---+---+---+
        || X | X |   |
        |+---+---+---+
        ||   |   | X |
        |+---+---+---+""".stripMargin
    gridString shouldBe expected

end SimulationDSLTest
