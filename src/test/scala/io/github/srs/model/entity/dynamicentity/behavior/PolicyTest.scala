package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.moveForward
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReading, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for `Policy` (name parsing and execution semantics).
 */
final class PolicyTest extends AnyFlatSpec with Matchers:

  private object C:
    val Seed: Long = 42L
    val CorrectStrings: Seq[String] = Seq("AlwaysForward", "RandomWalk")
    val IncorrectStrings: Seq[String] = Seq("alwaysforward", " RANDOMWALK ", "", "SomeOtherPolicy")
    val EmptyReadings: SensorReadings = Vector.empty[SensorReading[Sensor[Robot, Environment], Any]]

  private def ctx(seed: Long): BehaviorContext = BehaviorContext(C.EmptyReadings, SimpleRNG(seed))

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived
  given CanEqual[RNG, RNG] = CanEqual.derived

  "Policy" should "accept only exact names (case-sensitive)" in:
    val ok = C.CorrectStrings.forall(s => Policy.fromString(s).nonEmpty)
    val bad = C.IncorrectStrings.forall(s => Policy.fromString(s).isEmpty)

    (ok && bad) shouldBe true

  "Policy.AlwaysForward" should "produce moveForward and not advance the RNG" in:
    val simpleRNG = SimpleRNG(C.Seed)
    val (action, rngAfter) = Policy.AlwaysForward.run[Id](ctx(C.Seed))

    val ok = (action == moveForward[Id]) && (rngAfter == simpleRNG)
    ok shouldBe true

  "Policy.RandomWalk" should "advance the RNG and produce a non-null Action" in:
    val simpleRNG = SimpleRNG(C.Seed)
    val (randomAction, rngAfter) = Policy.RandomWalk.run[Id](ctx(C.Seed))

    val ok = (rngAfter != simpleRNG) && (randomAction != null)
    ok shouldBe true

  it should "be deterministic given the same seed" in:
    val (a1, r1) = Policy.RandomWalk.run[Id](ctx(C.Seed))
    val (a2, r2) = Policy.RandomWalk.run[Id](ctx(C.Seed))

    val same = (a1 == a2) && (r1 == r2)
    same shouldBe true
end PolicyTest
