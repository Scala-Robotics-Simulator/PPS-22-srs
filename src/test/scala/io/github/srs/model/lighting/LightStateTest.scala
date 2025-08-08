package io.github.srs.model.lighting

import scala.collection.immutable.ArraySeq

import io.github.srs.model.Cell
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.environment.{ view, Environment, EnvironmentView }
import io.github.srs.model.lighting.LightState
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*

class LightStateTest extends AnyFlatSpec:

  private val W = 3
  private val H = 2
  private val values = ArraySeq(0.1, 0.2, 0.3, 0.4, 0.5, 0.6)
  private val state = LightState.fromArray(W, values)

  private val obstaclePos = Point2D(1.0, 0.1)
  private val lightPos = Point2D(2.0, 0.2)
  private val robotPos = Point2D(0.5, 1.0)

  private val light = Light(
    lightPos,
    Orientation(0),
    radius = 0.1,
    illuminationRadius = 1.0,
    intensity = 1.0,
    attenuation = 0.0,
  )

  private val robot = Robot(
    robotPos,
    ShapeType.Circle(0.4),
    Orientation(0),
    Seq.empty,
  ).validate.toOption.value

  private val env: Environment = Environment(
    width = W,
    height = H,
    entities = Set(
      light,
      robot,
      Obstacle(obstaclePos, Orientation(0), 1.0, 0.1),
    ),
  ).validate.toOption.value

  private val view: EnvironmentView = env.view

  "LightState" should "expose the correct grid dimensions" in:
    (state.width, state.height) shouldBe (W, H)

  it should "return the stored intensities for in‑bounds cells" in:
    Seq(
      Cell(0, 0) -> 0.1,
      Cell(1, 0) -> 0.2,
      Cell(2, 0) -> 0.3,
      Cell(0, 1) -> 0.4,
      Cell(1, 1) -> 0.5,
      Cell(2, 1) -> 0.6,
    ).foreach { (cell, expected) =>
      state.intensity(cell) shouldBe expected
    }

  it should "return 0.0 for out‑of‑bounds cells" in:
    Seq(Cell(-1, 0), Cell(0, -1), Cell(3, 0), Cell(0, 2), Cell(3, 2))
      .foreach(cell => state.intensity(cell) shouldBe 0.0)

  it should "render as ASCII with '#'=obstacle, 'L'=light, 'R'=robot" in:
    val expected =
      """░▒L
        |▓R█""".stripMargin
    state.render(view, ascii = true) shouldBe expected

  it should "render as raw numbers with same overrides" in:
    val expected =
      """0.17 0.33 L
        |0.67 R 1.00""".stripMargin

    state.render(view, ascii = false) shouldBe expected

end LightStateTest
