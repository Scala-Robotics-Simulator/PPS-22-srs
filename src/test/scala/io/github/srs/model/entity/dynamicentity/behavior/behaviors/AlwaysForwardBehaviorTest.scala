package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.moveForward
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReading, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[AlwaysForwardBehavior]] behavior.
 */
final class AlwaysForwardBehaviorTest extends AnyFlatSpec with Matchers:

  private val seed = 123L

  private val emptyReadings: SensorReadings =
    Vector.empty[SensorReading[Sensor[?, Environment], Any]]

  given CanEqual[RNG, RNG] = CanEqual.derived
  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived

  "AlwaysForwardBehavior" should "return moveForward and keep the same RNG" in:
    val ctx = BehaviorContext(emptyReadings, SimpleRNG(seed))
    val (act, nextRng) = AlwaysForwardBehavior.decision[Id].run(ctx)
    val _ = act shouldBe moveForward[Id]
    nextRng shouldBe SimpleRNG(seed)
