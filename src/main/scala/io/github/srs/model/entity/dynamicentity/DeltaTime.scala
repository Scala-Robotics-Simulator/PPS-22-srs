package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.{ notInfinite, notNaN, positive }

/**
 * Represents a time step for dynamic entities, typically used in simulations.
 */
opaque type DeltaTime = Double

/**
 * Companion object for [[DeltaTime]] providing methods to create and validate delta time values.
 */
object DeltaTime:

  /**
   * Creates a new instance of [[DeltaTime]] if the provided value is positive.
   *
   * @param dt
   *   the time step in seconds.
   * @return
   *   a `Validation` containing the `DeltaTime` if valid, or an error if invalid.
   */
  def apply(dt: Double): Validation[DeltaTime] =
    for
      dt <- notNaN("dt", dt)
      dt <- notInfinite("dt", dt)
      dt <- positive("dt", dt)
    yield dt

  /**
   * Extension method to convert a `DeltaTime` to seconds.
   */
  extension (dt: DeltaTime) def toSeconds: Double = dt
end DeltaTime
