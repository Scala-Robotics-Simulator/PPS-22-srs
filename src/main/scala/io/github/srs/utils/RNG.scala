package io.github.srs.utils

trait RNG:
  def nextInt: (Int, RNG)
  def nextIntBetween(min: Int, max: Int): (Int, RNG)
  def nextDouble: (Double, RNG)
  def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG)
  def nextLong: (Long, RNG)
  def nextString(length: Int): (String, RNG)

final case class SimpleRNG(seed: Long) extends RNG:
  private val rng = new scala.util.Random(seed)
  private def nextRNG: RNG = SimpleRNG(rng.nextLong())

  override def nextInt: (Int, RNG) =
    val nextInt = rng.nextInt()
    (nextInt, nextRNG)

  override def nextIntBetween(min: Int, max: Int): (Int, RNG) =
    val nextInt = rng.between(min, max)
    (nextInt, nextRNG)

  override def nextDouble: (Double, RNG) =
    val nextDouble = rng.nextDouble()
    (nextDouble, nextRNG)

  override def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG) =
    val nextDouble = rng.nextDouble
    val scaled = min + (max - min) * nextDouble
    val result = if !isMaxExcluded && nextDouble > 0.999999999999999 then max else scaled
    (result, nextRNG)

  override def nextLong: (Long, RNG) =
    val nextLong = rng.nextLong()
    (nextLong, nextRNG)

  override def nextString(length: Int): (String, RNG) =
    val nextString = rng.nextString(length)
    (nextString, nextRNG)
end SimpleRNG
