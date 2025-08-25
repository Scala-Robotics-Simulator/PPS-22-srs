package io.github.srs.model.dsl

import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.language.postfixOps

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import io.github.srs.model.Simulation.simulation
import io.github.srs.model.dsl.SimulationDSL.on
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor.applyMovementActions
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.*
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.EqualityGivenInstances.given
import org.scalactic.Prettifier.default
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimulationDSLTest extends AnyFlatSpec with Matchers:

  val env: Environment = environment withWidth 3 withHeight 3

  "SimulationDSL" should "render an empty 3x3 grid" in:
    val gridString = simulation on env asGrid
    val expected =
      """+----+----+----+
        ||    |    |    |
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+""".stripMargin
    gridString shouldBe expected

  it should "render an environment 3x3 containing an obstacle" in:
    val updatedEnv = env containing (obstacle at (1, 1))
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+----+----+----+
        ||    |    |    |
        |+----+----+----+
        ||    | X0 |    |
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+""".stripMargin
    gridString shouldBe expected

  it should "render an environment 3x3 containing an obstacles 2x2" in:
    val updatedEnv = env containing (obstacle at (0, 0) withWidth 2 withHeight 2)
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+----+----+----+
        || X0 | X0 |    |
        |+----+----+----+
        || X0 | X0 |    |
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+""".stripMargin
    gridString shouldBe expected

  it should "render an environment 3x3 containing multiple obstacles" in:
    val updatedEnv =
      env containing (obstacle at (0, 0) withWidth 1 withHeight 2) and (obstacle at (2, 0)) and (obstacle at (2, 2))
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+----+----+----+
        || X0 |    | X1 |
        |+----+----+----+
        || X0 |    |    |
        |+----+----+----+
        ||    |    | X2 |
        |+----+----+----+""".stripMargin
    gridString shouldBe expected

  it should "render an environment 3x3 containing a robot" in:
    val updatedEnv = env containing (robot at (1, 1))
    val gridString = simulation on updatedEnv asGrid
    val expected =
      """+----+----+----+
        ||    |    |    |
        |+----+----+----+
        ||    | R0→|    |
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+""".stripMargin
    gridString shouldBe expected

  def stepEnv(env: Environment, dt: FiniteDuration): Environment =
    env.entities.collect { case r: Robot => r }.toList
      .foldLeftM(env) { (e, r) =>
        for
          sensorReadings <- r.senseAll[IO](e)
          action = r.behavior.run(sensorReadings)
          updatedRobot <- r.applyMovementActions[IO](dt, action)
          newEnv = e.copy(entities = e.entities - r + updatedRobot)
        yield newEnv
      }
      .unsafeRunSync()

  def runSteps(env: Environment, n: Int, dt: FiniteDuration = 10_000.millis): Environment =
    (0 until n).foldLeft(env) { (currentEnv, _) =>
      stepEnv(currentEnv, dt)
    }

  it should "render an environment 3x3 containing a robot moving forward" in:
    val updatedEnv =
      env containing (
        robot at (1, 1)
          withActuator (differentialWheelMotor ws 1.0)
          withBehavior Policy.AlwaysForward
      )

    val finalEnv = runSteps(updatedEnv, 1)

    val gridString = simulation on finalEnv asGrid
    val expected =
      """+----+----+----+
        ||    |    |    |
        |+----+----+----+
        ||    |    | R0→|
        |+----+----+----+
        ||    |    |    |
        |+----+----+----+""".stripMargin

    gridString shouldBe expected

end SimulationDSLTest
