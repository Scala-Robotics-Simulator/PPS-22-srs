package io.github.srs.model.entity

/**
 * Represents an orientation in a two-dimensional plane.
 *
 * The orientation is expressed in degrees but can also be converted to radians.
 */
trait Orientation:
  /**
   * The value of this orientation in degrees.
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

/**
 * Companion object for [[Orientation]], providing a factory method.
 */
object Orientation:
  /**
   * Creates a new [[Orientation]] instance from an angle in degrees.
   *
   * @param degree
   *   the angle in degrees.
   * @return
   *   a new [[Orientation]] representing the given angle.
   */
  def apply(degree: Double): Orientation = OrientationImpl(degree)

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
    override def toRadians: Double = math.toRadians(degree)
end Orientation
