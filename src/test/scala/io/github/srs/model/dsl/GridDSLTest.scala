package io.github.srs.model.dsl

import scala.language.postfixOps

import io.github.srs.config.run
import io.github.srs.model.Simulation.*
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
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GridDSLTest extends AnyFlatSpec with Matchers:

  val obstacleSize = 0.999999

  extension (env: Environment)

    infix def shouldEqualExceptIds(expectedEnv: Environment, tol: Double = 0.5): Boolean =
      val _ = env.width shouldEqual expectedEnv.width
      val _ = env.height shouldEqual expectedEnv.height

      println(s"Actual: ${env.entities}")
      println(s"Expected: ${expectedEnv.entities}")
      expectedEnv.entities.forall { e =>
        env.entities.exists { a =>
          val dx = math.abs(a.position.x - e.position.x)
          val dy = math.abs(a.position.y - e.position.y)

          val dw = (a, e) match
            case (ao: Obstacle, eo: Obstacle) => math.abs(ao.width - eo.width)
            case _ => 0.0
          val dh = (a, e) match
            case (ao: Obstacle, eo: Obstacle) => math.abs(ao.height - eo.height)
            case _ => 0.0

          dx <= tol && dy <= tol && dw <= tol && dh <= tol
        }
      }

    infix def shouldEqualExceptIdsStrict(expectedEnv: Environment, tol: Double = 1e-6): Boolean =
      expectedEnv.entities.forall { e =>
        env.entities.exists { a =>
          val dx = math.abs(a.position.x - e.position.x)
          val dy = math.abs(a.position.y - e.position.y)
          val dw = (a, e) match { case (ao: Obstacle, eo: Obstacle) => math.abs(ao.width - eo.width); case _ => 0.0 }
          val dh = (a, e) match { case (ao: Obstacle, eo: Obstacle) => math.abs(ao.height - eo.height); case _ => 0.0 }
          dx <= tol && dy <= tol && dw <= tol && dh <= tol
        }
      }

  def almostEqual(p1: Point2D, p2: Point2D, tol: Double = 0.5): Boolean =
    math.abs(p1.x - p2.x) <= tol && math.abs(p1.y - p2.y) <= tol

  private def neighborhood(pos: Point2D): Set[Point2D] =
    import io.github.srs.model.entity.Point2D.*
    (for {
      dx <- -1 to 1
      dy <- -1 to 1
    } yield Point2D(pos.x.toInt + dx, pos.y.toInt + dy)).toSet


  "GridDSL" should "create an empty environment" in:
    val env: Environment =
      -- | -- || -- | --
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
      (obstacle at Point2D(2.5, 0.5) withWidth obstacleSize withHeight obstacleSize) and
      (obstacle at Point2D(0.5, 2.5) withWidth obstacleSize withHeight obstacleSize) and
      (light at Point2D(3.5, 3.5))
    (env shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify that the robot of type A moves always forward" in:
    val env: Environment =
      -- | --  | -- | -- | -- ||
      A  | --  | -- | -- | -- ||
      -- | --  | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value

    val expectedEnv = (environment withWidth 5 withHeight 3 containing
      (robot at Point2D(4.5, 1.5) withSpeed 1.0 withBehavior Policy.AlwaysForward)
    ).validate.toOption.value

    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv run).value.environment
    (runSimulationEnv1 shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify the position of type O robot with avoids obstacle as behavior" in:
    val env: Environment =
      -- | X  | X  ||
      -- | O  | -- ||
      ** | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value

    val expectedEnv = (environment withWidth 3 withHeight 3 containing
      (obstacle at Point2D(1.5, 0.5) withWidth obstacleSize withHeight obstacleSize) and
      (obstacle at Point2D(2.5, 0.5) withWidth obstacleSize withHeight obstacleSize) and
      (robot at Point2D(2.5, 2.5)).withSpeed(1.0).withProximitySensors.withBehavior(Policy.ObstacleAvoidance) and
      (light at Point2D(0.5, 2.5))).validate.toOption.value

    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv run).value.environment
    (runSimulationEnv1 shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify the position of type R robot with random walk as behavior is the same with a fixed seed" in :
    val env: Environment =
      -- | X  | X  ||
      -- | R  | -- ||
      ** | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val expectedEnv = (environment withWidth 3 withHeight 3 containing
      (obstacle at Point2D(1.5, 0.5) withWidth obstacleSize withHeight obstacleSize) and
      (obstacle at Point2D(2.5, 0.5) withWidth obstacleSize withHeight obstacleSize) and
      (robot at Point2D(2.5, 1.5)).withSpeed(1.0).withProximitySensors.withBehavior(Policy.RandomWalk) and
      (light at Point2D(0.5, 2.5))).validate.toOption.value

    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv run).value.environment
    (runSimulationEnv1 shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify the simulation is different with different seeds with a robot of type R (RandomWalk)" in:
    val env: Environment =
      -- | --  | -- | -- | -- ||
      R  | --  | -- | -- | -- ||
      -- | --  | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value

    val expectedEnv1 = (environment withWidth 5 withHeight 3 containing
      (robot at Point2D(1.5, 1.5)).withSpeed(1.0).withProximitySensors.withBehavior(Policy.RandomWalk))
        .validate.toOption.value

    val expectedEnv2 = (environment withWidth 5 withHeight 3 containing
      (robot at Point2D(0.5, 1.5)).withSpeed(1.0).withProximitySensors.withBehavior(Policy.RandomWalk))
        .validate.toOption.value

    val runSimulationEnv1 = (simulation withDuration 10000 withSeed 42 in valEnv run).value.environment
    val runSimulationEnv2 = (simulation withDuration 10000 withSeed 0 in valEnv run).value.environment

    val _ = (runSimulationEnv1 shouldEqualExceptIds expectedEnv1) shouldBe true
    val _ = (runSimulationEnv2 shouldEqualExceptIds expectedEnv2) shouldBe true
    (runSimulationEnv1 shouldEqualExceptIdsStrict runSimulationEnv2) shouldBe false

  it should "verify that awaiting a tot amount of time the robot of type P (Phototaxis) moves towards the light" in :
    val env: Environment =
      -- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
      -- | -- | -- | P  | -- | -- | -- | X  | -- | -- ||
      -- | -- | ** | -- | -- | -- | -- | X  | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
      -- | -- | -- | -- | X  | X  | -- | X  | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | X  | -- | -- ||
      -- | P  | -- | -- | -- | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | ** | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val lightPos1 = Point2D(2, 2)
    val lightPos2 = Point2D(8, 8)

    val runSimulationEnv1 = (simulation withDuration 140000 withSeed 42 in valEnv run).value.environment

    val expectedRobotPos1 = neighborhood(lightPos1)
    val expectedRobotPos2 = neighborhood(lightPos2)

    val result = runSimulationEnv1.entities.exists:
      case robot: Robot =>
        expectedRobotPos1.exists(expected => almostEqual(robot.position, expected)) ||
          expectedRobotPos2.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true

  it should "verify the robot position with Mixed (Prioritized) behavior is near the light after a tot amount of time" in :
    val env: Environment =
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
      -- | -- | M  | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | X  | X  | -- | -- | ** | -- | -- ||
      -- | -- | -- | -- | -- | X  | X  | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --
    val valEnv: ValidEnvironment = env.validate.toOption.value
    val lightPos = Point2D(10, 5)

    val runSimulationEnv = (simulation withDuration 500000 withSeed 42 in valEnv run).value.environment

    val expectedRobotPos = neighborhood(lightPos)

    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        expectedRobotPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true
end GridDSLTest
