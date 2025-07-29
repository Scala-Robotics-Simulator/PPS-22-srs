package io.github.srs.behavior

import io.github.srs.model.behavior.Behavior
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.Action
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec

final class BehaviorTest extends AnyFlatSpec:

  import io.github.srs.model.behavior.Behavior.*
  import io.github.srs.model.entity.dynamicentity.Action.{MoveForward, Stop, TurnRight}
  import io.github.srs.model.entity.dynamicentity.sensor.*
  import org.scalatest.matchers.should.Matchers.shouldBe

  /** Build a dummy proximity sensor. */
  private def mkSensor() =
    ProximitySensor(Orientation(0.0), 0.1, 1.0).toOption.value

  /** SensorReadings with normalized proximity values (0..1). */
  private def readings(vals: Double*) =
    val s: ProximitySensor[?, ?] = mkSensor()
    val rs = vals.toVector.map(v => SensorReading(s, v))
    SensorReadings(proximity = rs)

  given CanEqual[Action, Action] = CanEqual.derived

  "Behavior.apply" should "lift a function into a Behavior" in:
    val b: Behavior[SensorReadings, Action] = Behavior(_ => Seq(Stop))
    b.execute(readings(0.9)) shouldBe Seq(Stop)

  "pure" should "always return the given action" in :
    pure[SensorReadings, Action](MoveForward).execute(readings(0.1, 0.2)) shouldBe Seq(MoveForward)

  "empty" should "produce no actions" in:
    empty[SensorReadings, Action].execute(readings(0.1, 0.2)) shouldBe Seq.empty[Action]

  "when(true)" should "emit the provided actions" in:
      when[SensorReadings, Action](_ => true)(Seq(TurnRight))
      .execute(readings(0.2)) shouldBe Seq(TurnRight)

  "when(false)" should "produce no actions" in:
      when[SensorReadings, Action](_ => false)(Seq(Action.TurnRight))
      .execute(readings(0.2)) shouldBe Seq.empty[Action]
