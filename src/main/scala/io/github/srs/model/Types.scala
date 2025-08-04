package io.github.srs.model

import io.github.srs.model.validation.Validation

opaque type PositiveDouble = Double

object PositiveDouble:

  def apply(value: Double): PositiveDouble =
    value

  extension (pd: PositiveDouble)
    /**
     * Converts a PositiveDouble to its underlying Double value.
     */
    def toDouble: Double = pd

    /**
     * Validates the PositiveDouble instance.
     *
     * @return
     *   [[Right]] if the instance is valid, or a [[Left]] with an error message if invalid.
     */
    def validate: Validation[PositiveDouble] =
      import io.github.srs.model.validation.Validation.{ notInfinite, notNaN, positive }
      for
        value <- notNaN("value", pd)
        value <- notInfinite("value", value)
        value <- positive("value", value)
      yield value
end PositiveDouble
