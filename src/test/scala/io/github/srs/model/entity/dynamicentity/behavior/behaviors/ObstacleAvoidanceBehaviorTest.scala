package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction, MovementActionFactory }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReading, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.Behaviors.ObstacleAvoidance.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.MinSpeed
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[ObstacleAvoidanceBehavior]] behavior.
 */
final class ObstacleAvoidanceBehaviorTest extends AnyFlatSpec with Matchers:

  private def P(deg: Double, v: Double) =
    SensorReading(ProximitySensor[DynamicEntity, Environment](Orientation(deg), 1.0), v)

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived

  given CanEqual[RNG, RNG] = CanEqual.derived

  given CanEqual[(Action[Id], RNG), (Action[Id], RNG)] = CanEqual.derived

  "ObstacleAvoidanceBehavior" should "go straight when everything is safe" in:
    val readings: SensorReadings = Vector(P(0, 1.0), P(90, 1.0), P(180, 1.0), P(270, 1.0))
    val (action, _) = ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(0)))
    action shouldBe MovementActionFactory.moveForward[Id]

  it should "turn toward clearer side when there is an obstacle ahead" in:

    val readings: SensorReadings = Vector(
      P(0, 0.5),
      P(90, 0.3),
      P(270, 0.95),
    )
    val (action, _) = ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(1)))
    inside(action):
      case MovementAction(l, r) =>
        val _ = l should be >= MinSpeed
        val _ = r should be >= MinSpeed
        l should be > r // turning right

  it should "pivot in place if obstacle is critically close" in:
    val readings: SensorReadings = Vector(
      P(0, math.min(0.05, CriticalDist / 2)),
      P(90, 0.8),
      P(270, 0.8),
    )
    val (action, _) = ObstacleAvoidanceBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(2)))
    inside(action):
      case MovementAction(l, r) =>
        (l * r) should be < 0.0 + 1e-9
end ObstacleAvoidanceBehaviorTest
