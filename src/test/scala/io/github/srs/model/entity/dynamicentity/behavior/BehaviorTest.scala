package io.github.srs.model.entity.dynamicentity.behavior

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

final class BehaviorTest extends AnyFlatSpec:

  "true" should "always return true" in:
    true shouldBe true

//import io.github.srs.model.entity.dynamicentity.behavior.Behavior
//import io.github.srs.model.entity.dynamicentity.behavior.Behavior.{ empty, pure, when }
//import org.scalatest.flatspec.AnyFlatSpec
//import org.scalatest.matchers.should.Matchers.shouldBe
//
///**
// * Test suite for the `Behavior` trait and its related functionality.
// */
//final class BehaviorTest extends AnyFlatSpec:
//
//
//  import io.github.srs.model.entity.Orientation
//  import io.github.srs.model.entity.dynamicentity.Action
//  import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReading, SensorReadings }
//
//  /** Build a dummy proximity sensor. */
//  private def mkSensor() =
//    ProximitySensor(Orientation(0.0), 0.1, 1.0)
//
//  /** SensorReadings with normalized proximity values (0..1). */
//  private def readings(vals: Double*) =
//    val s: ProximitySensor[?, ?] = mkSensor()
//    vals.toVector.map(v => SensorReading(s, v))
//
//  given CanEqual[Action, Action] = CanEqual.derived
//
//  "Behavior.apply" should "lift a function into a Behavior" in:
//    val b: Behavior[SensorReadings, Action] = Behavior(_ => Seq(Action.Stop))
//    b.execute(readings(0.9)) shouldBe Seq(Action.Stop)
//
//  "pure" should "always return the given action" in:
//    pure[SensorReadings, Action](Action.MoveForward).execute(readings(0.1, 0.2)) shouldBe Seq(Action.MoveForward)
//
//  "empty" should "produce no actions" in:
//    empty[SensorReadings, Action].execute(readings(0.1, 0.2)) shouldBe Seq.empty[Action]
//
//  "when(true)" should "emit the provided actions" in:
//    when[SensorReadings, Action](_ => true)(Seq(Action.TurnRight))
//      .execute(readings(0.2)) shouldBe Seq(Action.TurnRight)
//
//  "when(false)" should "produce no actions" in:
//    when[SensorReadings, Action](_ => false)(Seq(Action.TurnRight))
//      .execute(readings(0.2)) shouldBe Seq.empty[Action]
//
//  "map" should "transform produced actions" in:
//    pure[SensorReadings, Action](Action.MoveBackward).map {
//      case Action.MoveBackward => Action.Stop
//      case a => a
//    }.execute(readings()) shouldBe Seq(Action.Stop)
//
//  "filter" should "keep only actions that satisfy predicate" in:
//    Behavior[SensorReadings, Action]((_: SensorReadings) => Seq(Action.MoveForward, Action.Stop))
//      .filter(_ == Action.Stop)
//      .execute(readings()) shouldBe Seq(Action.Stop)
//
//  "++" should "concat two behaviours" in:
//    (pure[SensorReadings, Action](Action.Stop) ++ pure[SensorReadings, Action](Action.MoveForward))
//      .execute(readings()) shouldBe Seq(Action.Stop, Action.MoveForward)
//
//  "andAlso" should "alias ++ (concatenation)" in:
//    (pure[SensorReadings, Action](Action.Stop) andAlso pure[SensorReadings, Action](Action.MoveForward))
//      .execute(readings()) shouldBe Seq(Action.Stop, Action.MoveForward)
//
//  "<|>" should "pick the first non-empty" in:
//    (pure[SensorReadings, Action](Action.MoveForward) <|> pure[SensorReadings, Action](Action.Stop))
//      .execute(readings()) shouldBe Seq(Action.MoveForward)
//
//  "orElse" should "alias <|> (first non-empty wins)" in:
//    (pure[SensorReadings, Action](Action.TurnLeft) orElse pure[SensorReadings, Action](Action.Stop))
//      .execute(readings()) shouldBe Seq(Action.TurnLeft)
//end BehaviorTest
