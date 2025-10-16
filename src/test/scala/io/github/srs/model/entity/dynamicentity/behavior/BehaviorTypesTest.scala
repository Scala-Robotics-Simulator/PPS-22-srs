package io.github.srs.model.entity.dynamicentity.behavior

import cats.data.Kleisli
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for the BehaviorTypes aliases.
 */
final class BehaviorTypesTest extends AnyFlatSpec with Matchers:

  import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorTypes.*

  "Behavior alias" should "handle numeric input" in:
    val b: Behavior[Int, String] = Kleisli((i: Int) => (i * 2).toString)
    assert(b.run(5) == "10")

  "Condition alias" should "return false for negative numbers" in:
    val c: Condition[Int] = (i: Int) => i > 0
    assert(!c(-1))

  it should "return true for zero when condition is non-negative" in:
    val c: Condition[Int] = (i: Int) => i >= 0
    assert(c(0))
