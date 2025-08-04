package io.github.srs.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RandomTest extends AnyFlatSpec with Matchers:

  val seed: Long = 42L
  val rng: SimpleRNG = SimpleRNG(seed)

  "Random Number Generator" should "produce a deterministic value given the same seed" in:
    val rng1 = SimpleRNG(seed)
    val rng2 = SimpleRNG(seed)
    val (value1, nextRng1) = rng1.nextInt
    val (value2, nextRng2) = rng2.nextInt
    (value1, nextRng1) should be(value2, nextRng2)

  it should "produce a different value on successive calls given the same seed" in:
    val (firstValue, nextRNG) = rng.nextInt
    val (secondValue, _) = nextRNG.nextInt
    firstValue should not be secondValue

  it should "produce a double value in the range [0.0, 1.0)" in:
    val (doubleValue, _) = rng.nextDouble
    doubleValue should (be >= 0.0 and be < 1.0)

  it should "produce a double value in a specified range" in:
    val min = -1.0
    val max = 1.0
    val (doubleValue, _) = rng.nextDoubleBetween(min, max)
    doubleValue should (be >= min and be < max)

  it should "produce max at least once when max is included" in:
    val min = 0.999999999999999
    val max = 1.0
    val trials = 100_000
    val results = (1 to trials).map { _ =>
      val (d, _) = rng.nextDoubleBetween(min, max, isMaxExcluded = false)
      d
    }
    val foundMax = results.contains(max)
    foundMax shouldBe true

  it should "produce a string of specified length" in:
    val length = 10
    val (stringValue, _) = rng.nextString(length)
    stringValue.length should be(length)
end RandomTest
