package io.github.srs.lighting

import io.github.srs.model.*
import io.github.srs.model.entity.{ Entity, Orientation }
import io.github.srs.model.entity.staticentity.StaticEntity.*
import io.github.srs.model.environment.*
import io.github.srs.model.lighting.{ LightState, ShadowFovDiffuser }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class ShadowFovDiffuserTest extends AnyFlatSpec:

  private val GridSide = 5
  private val EmptyMap = LightState.empty(GridSide, GridSide)
  private val Diffuser = ShadowFovDiffuser()

  private def viewOf(entities: Entity*): EnvironmentView =
    Environment(GridSide, GridSide, entities.toSet)
      .fold(err => fail(s"Environment invalid in test‑fixture: $err"), _.view)

  "ShadowFovDiffuser" should "illuminate symmetric cells in an empty room" in:
    val light = Light((2.0, 2.0), Orientation(0), 4.0, 1.0, 0.0)
    val ls = Diffuser.step(viewOf(light))(EmptyMap)

    val symmetricPairs = List(
      Cell(1, 2) -> Cell(3, 2),
      Cell(2, 1) -> Cell(2, 3),
    )

    symmetricPairs.foreach { (a, b) =>
      ls.intensity(a).shouldBe(ls.intensity(b) +- 1e-9)
    }

  it should "cast a shadow behind an obstacle" in:
    val light = Light((0.0, 2.0), Orientation(0), 5.0, 1.0, 0.0)
    val wall = Obstacle((2.0, 2.0), Orientation(0), 1, 1)
    val ls = Diffuser.step(viewOf(light, wall))(EmptyMap)

    val beforeWall = ls.intensity(Cell(1, 2))
    val behindWall = ls.intensity(Cell(4, 2))

    (behindWall < beforeWall).shouldBe(true)
end ShadowFovDiffuserTest
