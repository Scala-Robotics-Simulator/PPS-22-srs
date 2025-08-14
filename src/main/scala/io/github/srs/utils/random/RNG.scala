package io.github.srs.utils.random

/**
 * A trait representing a Random Number Generator (RNG).
 */
trait RNG:

  /**
   * Generates a random boolean value along with the next RNG state.
   * @return
   *   a tuple containing a random boolean and the next RNG state.
   */
  def nextBoolean: (Boolean, RNG)

  /**
   * Generates a random integer value along with the next RNG state.
   * @return
   *   a tuple containing a random integer and the next RNG state.
   */
  def nextInt: (Int, RNG)

  /**
   * Generates a random integer value within a specified range (inclusive of min, exclusive of max).
   * @param min
   *   the minimum value (inclusive).
   * @param max
   *   the maximum value (exclusive).
   * @return
   *   a tuple containing a random integer and the next RNG state.
   */
  def nextIntBetween(min: Int, max: Int, isMaxExcluded: Boolean = true): (Int, RNG)

  /**
   * Generates a random double value within the range [0.0, 1.0) along with the next RNG state.
   * @return
   *   a tuple containing a random double and the next RNG state.
   */
  def nextDouble: (Double, RNG)

  /**
   * Generates a random double value within a specified range.
   * @param min
   *   the minimum value (inclusive).
   * @param max
   *   the maximum value (exclusive or inclusive based on isMaxExcluded).
   * @param isMaxExcluded
   *   whether the maximum value is excluded.
   * @return
   *   a tuple containing a random double and the next RNG state.
   */
  def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG)

  /**
   * Generates a random long value along with the next RNG state.
   * @return
   *   a tuple containing a random long and the next RNG state.
   */
  def nextLong: (Long, RNG)

  /**
   * Generates a random string of a specified length.
   * @param length
   *   the length of the random string to generate.
   * @return
   *   a tuple containing a random string and the next RNG state.
   */
  def nextString(length: Int): (String, RNG)

end RNG

/**
 * A simple implementation of the RNG trait using Scala's built-in Random class. This RNG generates random values based
 * on a seed.
 * @param seed
 *   the initial seed for the random number generator.
 * @note
 *   This RNG is deterministic, meaning it will produce the same sequence of random values for the same seed.
 */
final case class SimpleRNG(seed: Long) extends RNG:

  /**
   * A random number generator that uses Scala's Random class.
   */
  private val random = new scala.util.Random(seed)

  /**
   * Generates a new seed for the RNG.
   * @return
   *   a new random seed.
   */
  private def nextSeed: Long = random.nextLong()

  /**
   * Creates a new RNG instance with the next seed.
   * @return
   *   a new RNG instance with the updated seed.
   */
  private def nextRNG: RNG = SimpleRNG(nextSeed)

  /**
   * @inheritdoc
   */
  override def nextBoolean: (Boolean, RNG) =
    val nextBoolean = random.nextBoolean()
    (nextBoolean, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextInt: (Int, RNG) =
    val nextInt = random.nextInt()
    (nextInt, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextIntBetween(min: Int, max: Int, isMaxExcluded: Boolean = true): (Int, RNG) =
    val upperBound =
      if isMaxExcluded then max
      else if max == Int.MaxValue then max
      else max + 1
    val nextInt = random.between(min, upperBound)
    (nextInt, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextDouble: (Double, RNG) =
    val nextDouble = random.nextDouble()
    (nextDouble, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextDoubleBetween(min: Double, max: Double, isMaxExcluded: Boolean = true): (Double, RNG) =
    val nextDouble = random.nextDouble
    val value =
      if !isMaxExcluded && nextDouble > 0.999999999999999 then max
      else min + (max - min) * nextDouble
    (value, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextLong: (Long, RNG) =
    val nextLong = random.nextLong()
    (nextLong, nextRNG)

  /**
   * @inheritdoc
   */
  override def nextString(length: Int): (String, RNG) =
    val nextString = random.nextString(length)
    (nextString, nextRNG)
end SimpleRNG
