package io.github.srs.utils.random

trait RNG:
  def nextInt: (Int, RNG)
  def nextIntBetween(min: Int, max: Int): (Int, RNG)
  def nextDouble: (Double, RNG)
  def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG)
  def nextLong: (Long, RNG)
  def nextString(length: Int): (String, RNG)

final case class SimpleRNG(seed: Long) extends RNG:
  private val random = new scala.util.Random(seed)
  private def nextSeed: Long = random.nextLong()
  private def nextRNG: RNG = SimpleRNG(nextSeed)

  override def nextInt: (Int, RNG) =
    val nextInt = random.nextInt()
    (nextInt, nextRNG)

  override def nextIntBetween(min: Int, max: Int): (Int, RNG) =
    val nextInt = random.between(min, max)
    (nextInt, nextRNG)

  override def nextDouble: (Double, RNG) =
    val nextDouble = random.nextDouble()
    (nextDouble, nextRNG)

  override def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG) =
    val nextDouble = random.nextDouble
    val scaled = min + (max - min) * nextDouble
    val result = if !isMaxExcluded && nextDouble > 0.999999999999999 then max else scaled
    (result, nextRNG)

  override def nextLong: (Long, RNG) =
    val nextLong = random.nextLong()
    (nextLong, nextRNG)

  override def nextString(length: Int): (String, RNG) =
    val nextString = random.nextString(length)
    (nextString, nextRNG)
end SimpleRNG
