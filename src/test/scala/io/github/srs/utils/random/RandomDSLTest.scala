package io.github.srs.utils.random

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.utils.random.*
import io.github.srs.utils.random.RandomDSL.*

class RandomDSLTest extends AnyFlatSpec with Matchers:

  val seed: Long = 42L
  val rng: SimpleRNG = SimpleRNG(seed)

  "RandomDSL" should "produce a deterministic value given the same seed with different RNGs" in:
    val rng1 = SimpleRNG(seed)
    val rng2 = SimpleRNG(seed)
    val (firstDouble, _) = rng1 generate randomDouble
    val (secondDouble, _) = rng2 generate randomDouble
    firstDouble shouldBe secondDouble

  it should "generate different random values on subsequent calls" in:
    val (firstDouble, rng1) = rng generate randomDouble
    val (secondDouble, _) = rng1 generate randomDouble
    firstDouble should not be secondDouble

  it should "generate a pair of random doubles" in:
    val randPair: Rand[(Double, Double)] = for
      d1 <- randomDouble
      d2 <- randomDouble
    yield (d1, d2)
    val (pair, _) = randPair(rng)
    (pair._1, pair._2) shouldBe a[(Double, Double)]

  it should "generate a random boolean" in:
    val (bool, _) = rng generate randomBoolean
    bool shouldBe a[Boolean]

  it should "generate a random double in range" in:
    val min = 0.0
    val max = 1.0
    val range = min to max
    val (randDouble, _) = rng generate range.includeMax
    randDouble should (be >= min and be <= max)

  it should "generate a list of random integers" in:
    val randList: Rand[List[Int]] = 5 times randomInt
    val (list, _) = rng generate randList
    list should have size 5

  it should "generate a positive random integer" in:
    val (posInt, _) = rng generate positive
    posInt should be >= 0

  it should "generate a random string of specified length" in:
    val length = 10
    val (str, _) = length randomString rng
    str should have length length

  it should "generate a random long" in:
    val (longValue, _) = rng generate randomLong
    longValue shouldBe a[Long]
end RandomDSLTest
