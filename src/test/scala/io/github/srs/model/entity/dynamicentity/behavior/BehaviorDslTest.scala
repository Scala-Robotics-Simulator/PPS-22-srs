package io.github.srs.model.entity.dynamicentity.behavior

import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorTypes.{Behavior, Condition, PartialBehavior}
import io.github.srs.model.entity.dynamicentity.robot.behavior.dsl.BehaviorDsl.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for the Behavior DSL builders (`==>`, `|`, `default`).
 */
final class BehaviorDslTest extends AnyFlatSpec with Matchers:

  private object C:
    val isEven: Condition[Int] = _ % 2 == 0
    val gt10: Condition[Int] = _ > 10
    val isPositive: Condition[Int] = _ > 0
    val aPlus = "PLUS"
    val aMinus = "MINUS"

  given CanEqual[String, String] = CanEqual.derived
  given CanEqual[Option[String], Option[String]] = CanEqual.derived
  given CanEqual[PartialBehavior[Int, String], PartialBehavior[Int, String]] = CanEqual.derived

  // ------------------------------------------------------------------ ==>
  "The ==> builder" should "return Some when predicate is true" in:
    val rule: PartialBehavior[Int, String] = C.isEven ==> C.aPlus
    rule.run(2) shouldBe Some(C.aPlus)

  it should "return None when predicate is false" in:
    val rule: PartialBehavior[Int, String] = C.isEven ==> C.aPlus
    rule.run(3) shouldBe None

  "The | combinator" should "keep the first Some" in:
    val left: PartialBehavior[Int, String] = C.isEven ==> C.aPlus
    val right: PartialBehavior[Int, String] = Kleisli(_ => Some(C.aMinus))
    val r: PartialBehavior[Int, String] = left | right
    r.run(2) shouldBe Some(C.aPlus)

  it should "defer to the second rule when the first returns None" in:
    val right: PartialBehavior[Int, String] = Kleisli(_ => Some(C.aMinus))
    right.run(3) shouldBe Some(C.aMinus)

  "default" should "return the fallback when all rules defer" in:
    val r: PartialBehavior[Int, String] = C.isEven ==> C.aPlus
    val bh: Behavior[Int, String] = r.default("ZERO")
    bh.run(3) shouldBe "ZERO"

  "onlyIf" should "gate a rule with another predicate" in:
    val alwaysSome: PartialBehavior[Int, String] = Kleisli(_ => Some(C.aPlus))
    val gated: PartialBehavior[Int, String] = alwaysSome.onlyIf(C.gt10)
    gated.run(20) shouldBe Some(C.aPlus)

  "or" should "be commutative" in:
    val a = C.isEven or C.isPositive
    val b = C.isPositive or C.isEven
    a(-2) shouldBe b(-2)

  "De Morgan" should "hold: not(a and b) == not(a) or not(b)" in:
    val lhs = (C.isEven and C.isPositive).not(3)
    val rhs = (C.isEven.not or C.isPositive.not)(3)
    lhs shouldBe rhs
end BehaviorDslTest
