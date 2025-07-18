package io.github.srs.model.entity

/**
 * Represents an orientation in a two-dimensional plane.
 *
 * The orientation is expressed in degrees but can also be converted to radians.
 */
trait Orientation:
  /**
   * The value of this orientation in degrees, it assu.
   *
   * @return
   *   the orientation angle in degrees.
   */
  /**
   * The value of this orientation in degrees, in the range [0, 360).
   *
   * @return
   *   the orientation angle in degrees.
   */
  def degrees: Double

  /**
   * Converts this orientation to radians.
   *
   * @return
   *   the orientation angle in radians.
   */
  def toRadians: Double

end Orientation

/**
 * Companion object for [[Orientation]], providing a factory method.
 */
object Orientation:

  /**
   * Normalizes the given angle in degrees to the range [0, 360).
   *
   * @param degree
   *   the angle in degrees to normalize.
   * @return
   *   the normalized angle in degrees.
   */
  private def normalize(degree: Double): Double =
    val d = degree % 360
    if d < 0 then d + 360 else d

  /**
   * Creates a new [[Orientation]] instance from an angle in degrees.
   *
   * @param degree
   *   the angle in degrees.
   * @return
   *   a new [[Orientation]] representing the given angle.
   */
  def apply(degree: Double): Orientation = OrientationImpl(normalize(degree))

  /**
   * Creates a new [[Orientation]] instance from an angle in radians.
   *
   * @param radians
   *   the angle in radians.
   * @return
   *   a new [[Orientation]] representing the given angle.
   */
  def fromRadians(radians: Double): Orientation =
    OrientationImpl(normalize(Math.toDegrees(radians)))

  /**
   * Default implementation of [[Orientation]].
   *
   * @param degree
   *   the orientation angle in degrees.
   */
  private case class OrientationImpl(private val degree: Double) extends Orientation:
    /**
     * @inheritdoc
     */
    override def degrees: Double = degree

    /**
     * @inheritdoc
     */
    override def toRadians: Double = Math.toRadians(degree)
end Orientation
