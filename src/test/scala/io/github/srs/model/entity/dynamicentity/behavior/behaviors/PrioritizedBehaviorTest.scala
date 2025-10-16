package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors.*
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.Behaviors.Prioritized.{DangerDist, LightThreshold}
import io.github.srs.utils.random.{RNG, SimpleRNG}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[PrioritizedBehavior]] behavior.
 */
final class PrioritizedBehaviorTest extends AnyFlatSpec with Matchers:

  private def createProxReading(deg: Double, v: Double) =
    SensorReading(ProximitySensor[DynamicEntity, Environment](Orientation(deg), 1.0), v)

  private def createLightReading(deg: Double, v: Double) =
    SensorReading(LightSensor[DynamicEntity, Environment](Orientation(deg)), v)

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived
  given CanEqual[RNG, RNG] = CanEqual.derived

  "PrioritizedBehavior" should "delegate to ObstacleAvoidance when hazard is present" in:
    val readings: SensorReadings = Vector(
      createProxReading(0, DangerDist - 1e-3),
      createLightReading(90, 1.0),
    )
    val ctx = BehaviorContext(readings, SimpleRNG(10))
    val (chosen, rngA) = PrioritizedBehavior.decision[Id].run(ctx)
    val (oa, rngB) = ObstacleAvoidanceBehavior.decision[Id].run(ctx)
    val _ = chosen shouldBe oa
    rngA shouldBe rngB

  it should "delegate to Phototaxis when no hazard and light is strong" in:
    val readings: SensorReadings = Vector(
      createProxReading(0, 1.0),
      createLightReading(90, LightThreshold + 0.1),
    )
    val ctx = BehaviorContext(readings, SimpleRNG(11))
    val (chosen, rngA) = PrioritizedBehavior.decision[Id].run(ctx)
    val (ptx, rngB) = PhototaxisBehavior.decision[Id].run(ctx)
    val _ = chosen shouldBe ptx
    rngA shouldBe rngB

  it should "fallback to RandomWalk when no hazard and no light" in:
    val readings: SensorReadings = Vector(createProxReading(0, 1.0))
    val seed = 12L

    val ctxA = BehaviorContext(readings, SimpleRNG(seed))
    val ctxB = BehaviorContext(readings, SimpleRNG(seed))

    val (chosen, rngA) = PrioritizedBehavior.decision[Id].run(ctxA)
    val (rw, rngB) = RandomWalkBehavior.decision[Id].run(ctxB)

    val _ = chosen shouldBe rw
    rngA shouldBe rngB
end PrioritizedBehaviorTest
