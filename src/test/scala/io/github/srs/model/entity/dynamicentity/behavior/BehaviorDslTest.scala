package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.{ Behavior, Rule }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import io.github.srs.model.entity.dynamicentity.behavior.dsl.dsl.*

/**
 * Unit tests for the Behaviour DSL builders (`==>`, `|`, `default`).
 *
 * Each single assertion lives in its own `it` block to avoid the “unused value of type Assertion” compiler warning.
 */
final class BehaviorDslTest extends AnyFlatSpec:

  private val isEven: Condition[Int] = _ % 2 == 0
  private val gt10: Condition[Int] = _ > 10
  private val isPositive: Condition[Int] = _ > 0
  private val aPlus = "PLUS"
  private val aMinus = "MINUS"

  given CanEqual[String, String] = CanEqual.derived

  given CanEqual[Option[String], Option[String]] = CanEqual.derived

  given CanEqual[Rule[Id, Int, String], Rule[Id, Int, String]] = CanEqual.derived

  // ------------------------------------------------------------------ ==>
  "The ==> builder" should "return Some when predicate is true" in:
    val rule: Rule[Id, Int, String] = isEven ==> aPlus // F = Id
    rule.run(2) shouldBe Some(aPlus)

  it should "return None when predicate is false" in:
    val rule: Rule[Id, Int, String] = isEven ==> aPlus
    rule.run(3) shouldBe None

  "The | combinator" should "keep the first Some" in:
    val left: Rule[Id, Int, String] = isEven ==> aPlus
    val right: Rule[Id, Int, String] = Kleisli(_ => Some(aMinus))
    val r: Rule[Id, Int, String] = left | right
    r.run(2) shouldBe Some(aPlus)

  it should "defer to the second rule when the first returns None" in:
    val right: Rule[Id, Int, String] = Kleisli(_ => Some(aMinus))
    right.run(3) shouldBe Some(aMinus)

  "default" should "return the fallback when all Rules defer" in:
    val r: Rule[Id, Int, String] = isEven ==> aPlus
    val bh: Behavior[Id, Int, String] = r.default("ZERO")
    bh.run(3) shouldBe "ZERO"

  "mapAction" should "transform the decided action" in:
    val r: Rule[Id, Int, Int] = isEven ==> 1
    val rm: Rule[Id, Int, String] = r.mapAction(_.toString)
    rm.run(2) shouldBe Some("1")

  "onlyIf" should "gate a rule with another predicate" in:
    val alwaysSome: Rule[Id, Int, String] = Kleisli(_ => Some(aPlus))
    val gated: Rule[Id, Int, String] = alwaysSome.onlyIf(gt10)
    gated.run(20) shouldBe Some(aPlus)

  "or" should "be commutative" in:
    val a = isEven or isPositive
    val b = isPositive or isEven
    a(-2) shouldBe b(-2)

  "De Morgan" should "hold: not(a and b) == not(a) or not(b)" in:
    val lhs = (isEven and isPositive).not(3)
    val rhs = (isEven.not or isPositive.not)(3)
    lhs shouldBe rhs
end BehaviorDslTest
