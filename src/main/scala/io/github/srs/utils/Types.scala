package io.github.srs.utils

import io.github.srs.model.validation.Validation

opaque type PositiveDouble = Double

object PositiveDouble:

  /**
   * Creates a new PositiveDouble from a given value.
   *
   * @param value
   *   the value to create the PositiveDouble from.
   * @return
   *   a Validation containing the PositiveDouble if valid, or an error if invalid.
   */
  def apply(value: Double): Validation[PositiveDouble] =
    import io.github.srs.model.validation.Validation.{notInfinite, notNaN, positive}
    for
      value <- notNaN("value", value)
      value <- notInfinite("value", value)
      value <- positive("value", value)
    yield value

  /**
   * Converts a PositiveDouble to its underlying Double value.
   */
  extension (pd: PositiveDouble) def toDouble: Double = pd
end PositiveDouble
