package io.github.srs.model.entity.dynamicentity.behavior

import io.github.srs.model.entity.dynamicentity.behavior.RobotBehaviors.idle
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.OptionValues.*

/**
 * Test suite for the `RobotBehavior` functionality.
 */
final class RobotBehaviorTest extends AnyFlatSpec:

  import io.github.srs.model.entity.Orientation
  import io.github.srs.model.entity.dynamicentity.Action
  import io.github.srs.model.entity.dynamicentity.Action.Stop
  import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReading, SensorReadings }

  private def mkSensor() =
    ProximitySensor(Orientation(0.0), 0.1, 1.0).toOption.value

  private def readings(vals: Double*) =
    val s: ProximitySensor[?, ?] = mkSensor()
    val rs = vals.toVector.map(v => SensorReading(s, v))
    SensorReadings(proximity = rs)

  given CanEqual[Action, Action] = CanEqual.derived
  given CanEqual[Seq[Action], Seq[Action]] = CanEqual.derived

  "idle" should "always stop" in:
    idle.execute(readings(0.1)) shouldBe Seq(Stop)

end RobotBehaviorTest
