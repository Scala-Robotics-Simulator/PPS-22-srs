package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.{Action, MovementAction}
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors.ObstacleAvoidanceBehavior
import io.github.srs.model.entity.dynamicentity.sensor.{ProximitySensor, SensorReading, SensorReadings}
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.Behaviors.ObstacleAvoidance.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{MaxSpeed, MinSpeed}
import io.github.srs.utils.random.{RNG, SimpleRNG}
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[ObstacleAvoidanceBehavior]] behavior.
 */
final class ObstacleAvoidanceBehaviorTest extends AnyFlatSpec with Matchers:

  /** Helper to create a proximity sensor reading. */
  private def createProxReading(deg: Double, v: Double) =
    SensorReading(ProximitySensor[DynamicEntity, Environment](Orientation(deg), 1.0), v)

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived
  given CanEqual[RNG, RNG] = CanEqual.derived
  given CanEqual[(Action[Id], RNG), (Action[Id], RNG)] = CanEqual.derived

  "ObstacleAvoidanceBehavior" should "go forward when there are no obstacles" in:
    val readings: SensorReadings =
      Vector(
        createProxReading(0, 1.0),
        createProxReading(90, 1.0),
        createProxReading(180, 1.0),
        createProxReading(270, 1.0),
      )

    val (action, _) =
      ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(0)))

    inside(action):
      case MovementAction(l, r) =>
        val _ = l shouldBe r +- 1e-9
        val _ = l should be >= MinSpeed
        l should be <= MaxSpeed

  it should "turn toward the clearer side when there is an obstacle ahead" in:
    val mid = (CriticalDist + SafeDist) / 2.0
    val readings: SensorReadings = Vector(
      createProxReading(30, mid),
      createProxReading(90, 0.3),
      createProxReading(270, 0.95),
    )

    val (action, _) =
      ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(1)))

    inside(action):
      case MovementAction(l, r) =>
        val _ = l should be > r
        val _ = l should be >= MinSpeed
        r should be >= MinSpeed

  it should "pivot in place if there is an obstacle very close ahead" in:
    val criticalFront = math.max(1e-6, CriticalDist / 2.0)
    val readings: SensorReadings = Vector(
      createProxReading(30, criticalFront),
      createProxReading(90, 0.8),
      createProxReading(270, 0.8),
    )

    val (action, _) =
      ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(2)))

    inside(action):
      case MovementAction(l, r) =>
        (l * r) should be < 0.0 + 1e-9
end ObstacleAvoidanceBehaviorTest
