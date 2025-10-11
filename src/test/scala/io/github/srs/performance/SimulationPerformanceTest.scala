package io.github.srs.performance

import cats.effect.unsafe.implicits.global
import io.github.srs.>>>
import io.github.srs.model.Simulation.*
import io.github.srs.model.SimulationConfig.SimulationSpeed.SUPERFAST
import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{*, given}
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.{Entity, Point2D}
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class SimulationPerformanceTest extends AnyFlatSpec with Matchers {
  
  "Simulation" should "be able to simulate 30 robots at 10 fps" in :
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

  it should "run the simulation at maximum speed" in :
    val env: Environment =
      -- | -- | -- | -- | -- ||
      R  | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | --

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val duration = 100_000
    val res = (simulation withDuration duration withSeed 42 in valEnv >>>).unsafeRunSync().value
    res.simulationSpeed shouldBe SUPERFAST
}