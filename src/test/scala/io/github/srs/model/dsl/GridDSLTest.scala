package io.github.srs.model.dsl

import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{*, given}
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{Entity, Point2D}
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.testutils.EnvironmentTestUtils.*
import io.github.srs.utils.SimulationDefaults.GridDSL.ObstacleSize
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class GridDSLTest extends AnyFlatSpec with Matchers:

  "GridDSL" should "create an empty environment from a grid" in:
    val env: Environment =
      -- | -- ||
      -- | --

    val expectedEnv = environment withWidth 2 withHeight 2 containing (Set.empty: Set[Entity])
    env shouldEqual expectedEnv

  it should "create an environment from a grid" in:
    val env: Environment =
      -- | -- | X  | -- | -- ||
      -- | -- | -- | -- | -- ||
      X  | -- | -- | -- | -- ||
      -- | -- | -- | ** | -- ||
      -- | -- | -- | -- | --

    val expectedEnv = environment withWidth 5 withHeight 5 containing
      (obstacle at Point2D(2.5, 0.5) withWidth ObstacleSize withHeight ObstacleSize) and
      (obstacle at Point2D(0.5, 2.5) withWidth ObstacleSize withHeight ObstacleSize) and
      (light at Point2D(3.5, 3.5))
    (env shouldEqualExceptIds expectedEnv) shouldBe true

end GridDSLTest
