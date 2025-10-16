package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction }
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors.RandomWalkBehavior
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReading, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[RandomWalkBehavior]] behavior.
 */
final class RandomWalkBehaviorTest extends AnyFlatSpec with Matchers:

  private val seed = 777L

  private val emptyReadings: SensorReadings =
    Vector.empty[SensorReading[Sensor[?, Environment], Any]]

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived
  given CanEqual[RNG, RNG] = CanEqual.derived
  given CanEqual[(Action[Id], RNG), (Action[Id], RNG)] = CanEqual.derived

  "RandomWalkBehavior" should "advance RNG and produce speeds within bounds" in:
    val ctx = BehaviorContext(emptyReadings, SimpleRNG(seed))
    val (action, nextRng) = RandomWalkBehavior.decision[Id].run(ctx)

    val _ = nextRng should not be SimpleRNG(seed)
    inside(action):
      case MovementAction(l, r) =>
        val _ = l should be >= MinSpeed
        val _ = l should be <= MaxSpeed
        val _ = r should be >= MinSpeed
        r should be <= MaxSpeed

  it should "be deterministic with the same seed" in:
    import Tuple.canEqualTuple
    val ctx1 = BehaviorContext(emptyReadings, SimpleRNG(seed))
    val ctx2 = BehaviorContext(emptyReadings, SimpleRNG(seed))
    val out1 = RandomWalkBehavior.decision[Id].run(ctx1)
    val out2 = RandomWalkBehavior.decision[Id].run(ctx2)
    out1 shouldBe out2

  it should "rarely be perfectly straight (turn bias active)" in:
    // Single sample: assert “not straight” to avoid the old vibration behaviour
    val ctx = BehaviorContext(emptyReadings, SimpleRNG(seed))
    val (action, _) = RandomWalkBehavior.decision[Id].run(ctx)
    inside(action):
      case MovementAction(l, r) =>
        (math.abs(l - r) > 1e-9) shouldBe true
end RandomWalkBehaviorTest
