package io.github.srs.model.dsl

import scala.language.postfixOps

import io.github.srs.model.dsl.Cell.{ --, X }
import io.github.srs.model.dsl.GridDSL.{ *, given }
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GridDSLTest extends AnyFlatSpec with Matchers:

  extension (env: Environment)

    infix def shouldEqualExceptIds(expectedEnv: Environment): Unit =
      val _ = env.width shouldEqual expectedEnv.width
      val _ = env.height shouldEqual expectedEnv.height
      env.entities.map(e => (e.position, e.orientation)) shouldEqual expectedEnv.entities.map(e =>
        (e.position, e.orientation),
      ): Unit

  "GridDSL" should "create an empty environment" in:
    val env: Environment =
      -- | -- || -- | --
    val expectedEnv = environment withWidth 2 withHeight 2 containing (Set.empty: Set[Entity])
    env shouldEqual expectedEnv

  it should "create an environment from a grid" in:
    val env: Environment =
      -- | -- | X ||
        -- | -- | -- ||
        X | -- | --
    val expectedEnv = environment withWidth 3 withHeight 3 containing
      (obstacle at Point2D(2, 0)) and (obstacle at Point2D(0, 2))
    env shouldEqualExceptIds expectedEnv
end GridDSLTest
