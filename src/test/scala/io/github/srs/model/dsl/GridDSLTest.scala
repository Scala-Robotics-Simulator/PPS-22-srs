package io.github.srs.model.dsl

import scala.language.postfixOps

import cats.effect.unsafe.implicits.global
import io.github.srs.>>>
import io.github.srs.model.Simulation.*
import io.github.srs.model.SimulationConfig.SimulationSpeed.SUPERFAST
import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{*, given}
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.StaticEntity.Obstacle
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{Entity, Point2D}
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.SimulationDefaults.GridDSL.ObstacleSize
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GridDSLTest extends AnyFlatSpec with Matchers:

  private def entitiesAlmostEqual(a: Entity, e: Entity, tol: Double): Boolean =
    val samePos = almostEqual(a.position, e.position, tol)
    val sameDims = (a, e) match
      case (ao: Obstacle, eo: Obstacle) =>
        math.abs(ao.width - eo.width) <= tol &&
        math.abs(ao.height - eo.height) <= tol
      case _ => true
    samePos && sameDims

  private def envAlmostEqual(env: Environment, expected: Environment, tol: Double): Boolean =
    env.width == expected.width &&
      env.height == expected.height &&
      expected.entities.forall { e =>
        env.entities.exists(a => entitiesAlmostEqual(a, e, tol))
      }

  private def almostEqual(p1: Point2D, p2: Point2D, tol: Double = 0.5): Boolean =
    math.abs(p1.x - p2.x) <= tol && math.abs(p1.y - p2.y) <= tol

  private def neighborhood(pos: Point2D): Set[Point2D] =
    import io.github.srs.model.entity.Point2D.*
    (for
      dx <- -1 to 1
      dy <- -1 to 1
    yield Point2D(pos.x.toInt + dx, pos.y.toInt + dy)).toSet

  extension (env: Environment)

    infix def shouldEqualExceptIds(expectedEnv: Environment, tol: Double = 0.5): Boolean =
      envAlmostEqual(env, expectedEnv, tol)

    infix def shouldEqualExceptIdsStrict(expectedEnv: Environment, tol: Double = 1e-6): Boolean =
      envAlmostEqual(env, expectedEnv, tol)

  "GridDSL" should "create an empty environment from a grid" in:
    val env: Environment =
      -- | -- ||
        -- | --

    val expectedEnv = environment withWidth 2 withHeight 2 containing (Set.empty: Set[Entity])
    env shouldEqual expectedEnv

  it should "create an environment from a grid" in:
    val env: Environment =
      -- | -- | X | -- | -- ||
        -- | -- | -- | -- | -- ||
        X | -- | -- | -- | -- ||
        -- | -- | -- | ** | -- ||
        -- | -- | -- | -- | --

    val expectedEnv = environment withWidth 5 withHeight 5 containing
      (obstacle at Point2D(2.5, 0.5) withWidth ObstacleSize withHeight ObstacleSize) and
      (obstacle at Point2D(0.5, 2.5) withWidth ObstacleSize withHeight ObstacleSize) and
      (light at Point2D(3.5, 3.5))
    (env shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify that a type A robot with AlwaysForward policy moves moves straight ahead until the boundary" in:
    val env: Environment =
      -- | -- | -- | -- | -- ||
        A | -- | -- | -- | -- ||
        -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val expectedEnv = (environment withWidth 5 withHeight 3 containing
      (robot at Point2D(4.5, 1.5) withSpeed 1.0 withBehavior Policy.AlwaysForward)).validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify that a type O robot with ObstacleAvoidance policy avoids adjacent cells of obstacles" in:
    val env: Environment =
      -- | X | X ||
        -- | O | -- ||
        ** | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val obstaclePos1 = Point2D(1.5, 0.5)
    val obstaclePos2 = Point2D(2.5, 0.5)
    val forbiddenCellPos = neighborhood(obstaclePos1) ++ neighborhood(obstaclePos2)
    val runSimulationEnv = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        forbiddenCellPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe false

  it should "verify reproducibility of RandomWalk for type R robot with a fixed seed" in:
    val env: Environment =
      -- | X | X | -- | -- ||
        R | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        ** | -- | -- | -- | -- ||
        -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val runSimulationEnv2 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIdsStrict runSimulationEnv2) shouldBe true

  it should "produce different positions for a type R robot with RandomWalk when using different seeds" in:
    val env: Environment =
      -- | -- | -- | -- | -- ||
        R | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 150000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val runSimulationEnv2 =
      (simulation withDuration 150000 withSeed 1234 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIdsStrict runSimulationEnv2) shouldBe false

  it should "verify that a type P robot with Phototaxis ends up near a light source" in:
    val env: Environment =
      -- | -- | -- | -- | -- | -- | -- | X | -- | -- ||
        -- | -- | -- | P | -- | -- | -- | X | -- | -- ||
        -- | -- | ** | -- | -- | -- | -- | X | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | X | -- | -- ||
        -- | -- | -- | -- | X | X | -- | X | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | X | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | X | -- | -- ||
        -- | P | -- | -- | -- | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | ** | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val lightPos1 = Point2D(2, 2)
    val lightPos2 = Point2D(8, 8)
    val expectedRobotPos = neighborhood(lightPos1) ++ neighborhood(lightPos2)
    val runSimulationEnv = (simulation withDuration 140000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        expectedRobotPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true

  it should "verify that Mixed behavior (Phototaxis + ObstacleAvoidance + RandomWalk) leads the robot near the light" in:
    val env: Environment =
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
        -- | -- | M | -- | -- | X | X | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | X | X | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | X | X | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | X | X | -- | -- | ** | -- | -- ||
        -- | -- | -- | -- | -- | X | X | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val lightPos = Point2D(10, 5)
    val expectedRobotPos = neighborhood(lightPos)
    val runSimulationEnv = (simulation withDuration 450000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        expectedRobotPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true

  it should "be able to simulate 30 robots at 10 fps" in:
    val robots: Set[Entity] =
      (for
        x <- 0 until 10
        y <- 0 until 3
      yield robot at Point2D(x + 0.5, y + 0.5) withSpeed 1.0 withBehavior Policy.RandomWalk).toSet

    val env = (environment withWidth 10 withHeight 10 containing robots).validate.toOption.value
    val duration = 100_000
    val start = System.currentTimeMillis()
    val _ = (simulation withDuration duration withSeed 42 in env >>>).unsafeRunSync()
    val end = System.currentTimeMillis()
    val totalTime = end - start
    println(s"Total time for $duration ms of simulation: $totalTime ms")
    totalTime should be <= duration.toLong

  it should "run the simulation at maximum speed" in:
    val env: Environment =
      -- | -- | -- | -- | -- ||
        R | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val duration = 100_000
    val res = (simulation withDuration duration withSeed 42 in valEnv >>>).unsafeRunSync().value
    res.simulationSpeed shouldBe SUPERFAST

end GridDSLTest
