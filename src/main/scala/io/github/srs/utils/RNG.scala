package io.github.srs.utils

trait RNG:
  def nextInt: (Int, RNG)
  def nextDouble: (Double, RNG)
  def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG)
  def nextLong: (Long, RNG)
  def nextString(length: Int): (String, RNG)

final case class SimpleRNG(seed: Long) extends RNG:
  private val rng = new scala.util.Random(seed)
  private def nextRNG: RNG = SimpleRNG(rng.nextLong())

  override def nextInt: (Int, RNG) =
    val nextValue = rng.nextInt()
    (nextValue, nextRNG)

  override def nextDouble: (Double, RNG) =
    val nextValue = rng.nextDouble()
    (nextValue, nextRNG)

  override def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG) =
    val base = rng.nextDouble
    val scaled = min + (max - min) * base
    val result = if !isMaxExcluded && base > 0.999999999999999 then max else scaled
    (result, nextRNG)

  override def nextLong: (Long, RNG) =
    val nextValue = rng.nextLong()
    (nextValue, nextRNG)

  override def nextString(length: Int): (String, RNG) =
    val nextValue = rng.nextString(length)
    (nextValue, nextRNG)
end SimpleRNG
