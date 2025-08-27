package io.github.srs.model.illumination.raster

/**
 * Represents the occlusion value of a grid cell.
 *
 * Occlusion values range from 0.0 (fully transparent) to 1.0 (fully occluded).
 */
opaque type OpacityValue = Double

/**
 * Companion object providing utility methods and extensions for [[OcclusionValue]].
 */
object OpacityValue:
  /**
   * Fully occluded cell value (1.0)
   */
  val Occluded: OpacityValue = 1.0

  /**
   * Fully cleared cell value (0.0)
   */
  val Cleared: OpacityValue = 0.0

  extension (v: OpacityValue)
    /**
     * Gets the underlying double value of this [[OcclusionValue]].
     *
     * @return
     *   The raw double value
     */
    def value: Double = v

  /**
   * Implicit conversion from [[OcclusionValue]] to `Double` for integration with math operations.
   */
  given Conversion[OpacityValue, Double] with
    inline def apply(v: OpacityValue): Double = v

  given CanEqual[OpacityValue, OpacityValue] = CanEqual.derived

end OpacityValue
