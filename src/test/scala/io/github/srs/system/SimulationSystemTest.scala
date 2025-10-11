package io.github.srs.system

import cats.effect.unsafe.implicits.global
import io.github.srs.>>>
import io.github.srs.model.Simulation.*
import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{*, given}
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.Point2D
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.testutils.EnvironmentTestUtils.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.convertOptionToValuable

import scala.language.postfixOps

class SimulationSystemTest extends AnyFlatSpec with Matchers:

  "Simulation system" should "verify that a type A robot with AlwaysForward policy moves moves straight ahead until the boundary" in :
    val env: Environment =
      -- | -- | -- | -- | -- ||
      A  | -- | -- | -- | -- ||
      -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val expectedEnv = (environment withWidth 5 withHeight 3 containing
      (robot at Point2D(4.5, 1.5) withSpeed 1.0 withBehavior Policy.AlwaysForward)).validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIds expectedEnv) shouldBe true

  it should "verify that a type O robot with ObstacleAvoidance policy avoids adjacent cells of obstacles" in :
    val env: Environment =
      -- | X  | X  ||
      -- | O  | -- ||
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

  it should "verify reproducibility of RandomWalk for type R robot with a fixed seed" in :
    val env: Environment =
      -- | X  | X  | -- | -- ||
      R  | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      ** | -- | -- | -- | -- ||
      -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val runSimulationEnv2 = (simulation withDuration 60000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIdsStrict runSimulationEnv2) shouldBe true

  it should "produce different positions for a type R robot with RandomWalk when using different seeds" in :
    val env: Environment =
      -- | -- | -- | -- | -- ||
      R  | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val runSimulationEnv1 = (simulation withDuration 150000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val runSimulationEnv2 =
      (simulation withDuration 150000 withSeed 1234 in valEnv >>>).unsafeRunSync().value.environment
    (runSimulationEnv1 shouldEqualExceptIdsStrict runSimulationEnv2) shouldBe false

  it should "verify that a type P robot with Phototaxis ends up near a light source" in :
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
    val expectedRobotPos = neighborhood(lightPos1) ++ neighborhood(lightPos2)
    val runSimulationEnv = (simulation withDuration 140000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        expectedRobotPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true

  it should "verify that Mixed behavior (Phototaxis + ObstacleAvoidance + RandomWalk) leads the robot near the light" in :
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
    val expectedRobotPos = neighborhood(lightPos)
    val runSimulationEnv = (simulation withDuration 450000 withSeed 42 in valEnv >>>).unsafeRunSync().value.environment
    val result = runSimulationEnv.entities.exists:
      case robot: Robot =>
        expectedRobotPos.exists(expected => almostEqual(robot.position, expected))
      case _ => false
    result shouldBe true

