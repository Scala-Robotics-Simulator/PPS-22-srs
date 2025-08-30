package io.github.srs.model.dsl

import scala.language.postfixOps

import io.github.srs.model.Simulation.*
import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{ *, given }
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.convertOptionToValuable
import io.github.srs.config.run
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*

class GridDSLTest extends AnyFlatSpec with Matchers:

//  extension (env: Environment)
//
//    infix def shouldEqualExceptIds(expectedEnv: Environment): Unit =
//      val _ = env.width shouldEqual expectedEnv.width
//      val _ = env.height shouldEqual expectedEnv.height
//      env.entities.map(e => (e.position, e.orientation)) shouldEqual expectedEnv.entities.map(e =>
//        (e.position, e.orientation),
//      ): Unit

  extension (env: Environment)

    infix def shouldEqualExceptIds(expectedEnv: Environment, tol: Double = 0.3): Unit =
      val _ = env.width shouldEqual expectedEnv.width
      val _ = env.height shouldEqual expectedEnv.height

      def truncate1(d: Double): Double = (d * 10).toInt / 10.0

      val _ = expectedEnv.entities.forall { e =>
        env.entities.exists { a =>
          math.abs(truncate1(a.position.x) - truncate1(e.position.x)) <= tol &&
          math.abs(truncate1(a.position.y) - truncate1(e.position.y)) <= tol &&
          math.abs(a.orientation.degrees - e.orientation.degrees) <= tol
        }
      } shouldEqual true

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
      (obstacle at Point2D(2.5, 0.5)) and (obstacle at Point2D(0.5, 2.5))
    env shouldEqualExceptIds expectedEnv

  it should "create an environment with the robot positioned correctly after run simulation" in:
    val env: Environment =
      -- | X | X ||
        -- | R | -- ||
        ** | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value

    val expectedEnv = (environment withWidth 3 withHeight 3 containing
      (obstacle at Point2D(1.5, 0.5)) and
      (obstacle at Point2D(2.5, 0.5)) and
      (robot at Point2D(2.5, 1.5)) and
      (light at Point2D(0.5, 2.5))).validate.toOption.value
    (simulation withDuration 60000 withSeed 42 in valEnv run).value.environment shouldEqualExceptIds expectedEnv
end GridDSLTest
