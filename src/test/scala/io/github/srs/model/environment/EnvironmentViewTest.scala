package io.github.srs.model.environment

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.OptionValues._
import io.github.srs.model._
import io.github.srs.model.StaticEntity._

class EnvironmentViewTest extends AnyFlatSpec:

  given CanEqual[StaticEntity, StaticEntity] = CanEqual.derived

  //––– Scenario constants ––––––––––––––––––––––––––––––––––––––––––––––––
  private val W = 10
  private val H = 10

  private val obsOrigin = Point2D(1.0, 1.0)
  private val obstacleWidth = 2
  private val obstacleHeight = 1

  private val lightOrigin    = Point2D(4.0, 4.0)
  private val lightRadius    = 3.0
  private val lightIntensity = 1.0
  private val lightAttenuation     = 0.2

  // Pre‑compute exactly which cells should be occupied by the obstacle
  private val obstacleCells: Set[Cell] =
    val tl = obsOrigin.toCell
    (for
      dx <- 0 until obstacleWidth
      dy <- 0 until obstacleHeight
    yield Cell(tl.x + dx, tl.y + dy)).toSet

  private val expectedLight =
    Light(lightOrigin, Orientation(0), lightRadius, lightIntensity, lightAttenuation)

  // Build Environment
  private val env: Environment =
    Environment(W, H, Set(
      Obstacle(obsOrigin, Orientation(0), obstacleWidth, obstacleHeight),
      expectedLight
    )).toOption.value  // fails if invalid
  
  private val view: EnvironmentView = env.view

  "EnvironmentView.static" should "report the correct dimensions" in:
    (view.width, view.height) shouldBe (W, H)

  it should "contain exactly the obstacle cells" in:
    view.obstacles should contain theSameElementsAs obstacleCells

  it should "not include any light cell among the obstacles" in:
    view.obstacles should not contain lightOrigin.toCell

  it should "list exactly the static lights declared" in:
    view.lights should contain theSameElementsAs Vector(expectedLight)

  it should "produce a resistance grid with correct values" in:
    val grid = view.resistance

    // free cells is 0.0
    Seq(Cell(0,0), Cell(3,3), Cell(W-1, H-1)).foreach { c =>
      grid(c.x)(c.y) shouldBe 0.0
    }

    // obstacle cells is 1.0
    obstacleCells.foreach { c =>
      grid(c.x)(c.y) shouldBe 1.0
    }
