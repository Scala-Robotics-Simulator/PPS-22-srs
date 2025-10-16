package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import cats.Id
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementAction, MovementActionFactory }
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors.PhototaxisBehavior
import io.github.srs.model.entity.dynamicentity.sensor.{ LightSensor, SensorReading, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.MinSpeed
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite for the [[PhototaxisBehavior]] behavior.
 */
final class PhototaxisBehaviorTest extends AnyFlatSpec with Matchers:

  /** Helper to create a light sensor reading. */
  private def createLightReading(deg: Double, v: Double) =
    SensorReading(LightSensor[DynamicEntity, Environment](Orientation(deg)), v)

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived
  given CanEqual[RNG, RNG] = CanEqual.derived

  "PhototaxisBehavior" should "go forward when there are no lights" in:
    val readings: SensorReadings = Vector.empty
    val ctx = BehaviorContext(readings, SimpleRNG(0))
    val (action, _) = PhototaxisBehavior.decision[Id].run(ctx)
    action shouldBe MovementActionFactory.moveForward[Id]

  it should "turn toward the strongest light" in:
    val readings: SensorReadings = Vector(
      createLightReading(0, 0.2),
      createLightReading(90, 0.9),
      createLightReading(270, 0.1),
    )
    val (action, _) = PhototaxisBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(1)))
    inside(action):
      case MovementAction(l, r) =>
        val _ = l should be >= MinSpeed
        val _ = r should be >= MinSpeed
        r should be > l

  it should "pivot in place if the brightest light is almost directly behind" in:
    val readings: SensorReadings = Vector(
      createLightReading(180, 1.0),
      createLightReading(0, 0.1),
    )
    val (action, _) = PhototaxisBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(2)))
    inside(action):
      case MovementAction(l, r) =>
        (l * r) should be < 0.0 + 1e-9

  it should "move forward when the strongest light is directly ahead" in:
    val readings: SensorReadings = Vector(
      createLightReading(0, 1.0),
      createLightReading(90, 0.5),
      createLightReading(270, 0.5),
    )
    val (action, _) = PhototaxisBehavior.decision[Id].run(BehaviorContext(readings, SimpleRNG(3)))
    action shouldBe MovementActionFactory.moveForward[Id]

end PhototaxisBehaviorTest
