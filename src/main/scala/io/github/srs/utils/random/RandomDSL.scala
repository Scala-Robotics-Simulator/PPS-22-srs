package io.github.srs.utils.random

/**
 * A domain-specific language (DSL) for generating random values using a [[Rand]] type.
 */
object RandomDSL:

  /**
   * Generates a random integer value using the provided [[RNG]].
   * @return
   *   a [[Rand]] that generates a random integer.
   */
  infix def randomInt: Rand[Int] = (rng: RNG) => rng.nextInt

  /**
   * Generates a random positive integer value using the provided [[RNG]].
   * @return
   *   a [[Rand]] that generates a random positive integer.
   */
  infix def positive: Rand[Int] = (rng: RNG) => rng.nextIntBetween(0, Int.MaxValue)

  /**
   * Generates a random boolean value using the provided [[RNG]].
   * @return
   *   a [[Rand]] that generates a random boolean.
   */
  infix def randomBoolean: Rand[Boolean] = (rng: RNG) => rng.nextBoolean

  /**
   * Generates a double value within the range [0.0, 1.0) using the provided [[RNG]].
   * @return
   *   a [[Rand]] that generates a random double.
   */
  infix def randomDouble: Rand[Double] = (rng: RNG) => rng.nextDouble

  /**
   * Generates a long value using the provided [[RNG]].
   * @return
   *   a [[Rand]] that generates a random long.
   */
  infix def randomLong: Rand[Long] = (rng: RNG) => rng.nextLong

  extension (rng: RNG)
    /**
     * Generates a random value using the provided [[RNG]].
     * @param rand
     *   the [[Rand]] to use for generating the random value.
     * @tparam A
     *   the type of the random value to generate.
     * @return
     *   a tuple containing the generated random value and the next state of the [[RNG]].
     */
    infix def generate[A](rand: Rand[A]): (A, RNG) = rand(rng)

  extension (i: Int)
    /**
     * Generates a list of random values of type `A` using the provided [[Rand]] and the specified size `i`.
     * @param ra
     *   the [[Rand]] to use for generating the random values.
     * @tparam A
     *   the type of the random values to generate.
     * @return
     *   a [[Rand]] that generates a list of random values of type `A` of size `i`.
     */
    infix def times[A](ra: Rand[A]): Rand[List[A]] = Rand.listOfN(i, ra)

    /**
     * Generates a random string of length `i` using the provided [[RNG]].
     * @return
     *   a [[Rand]] that generates a random string of length `i`.
     */
    infix def randomString: Rand[String] = rng => rng.nextString(i)

  extension (start: Double)

    /**
     * Generates a random double value within the range [start, end) or [start, end] based on the `isMaxExcluded`
     * parameter.
     * @param end
     *   the end of the range (exclusive or inclusive based on `isMaxExcluded`).
     * @return
     *   a function that takes a boolean indicating whether the maximum value is excluded and returns a [[Rand]] that
     *   generates a random double.
     */
    infix def to(end: Double): Boolean => Rand[Double] =
      (isMaxExcluded: Boolean) => (rng: RNG) => rng.nextDoubleBetween(start, end, isMaxExcluded)

  extension (f: Boolean => Rand[Double])
    /**
     * Generates a random double value within the range [start, end] including the maximum value.
     * @return
     *   a [[Rand]] that generates a random double value, including the maximum value.
     */
    infix def includeMax: Rand[Double] = f(false)

    /**
     * Generates a random double value within the range [start, end) excluding the maximum value.
     * @return
     *   a [[Rand]] that generates a random double value, excluding the maximum value.
     */
    infix def excludeMax: Rand[Double] = f(true)

  extension [A](seq: Seq[A])
    /**
     * Generates a shuffled version of the list using the provided [[RNG]].
     *
     * @return
     *   a [[Rand]] that produces a tuple: the shuffled list and the next RNG state.
     */
    infix def shuffle: Rand[Seq[A]] = rng => rng.shuffle(seq)
end RandomDSL
