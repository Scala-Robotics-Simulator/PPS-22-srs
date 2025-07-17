package io.github.srs.model.environment

/**
 * Represents the environment in which entities exist.
 *
 * The environment is defined by its width and height, which are used to constrain the movement and positioning of
 * entities within it.
 */
trait Environment:
  /**
   * The width of the environment.
   *
   * @return
   *   a Double representing the width of the environment.
   */
  val width: Double

  /**
   * The height of the environment.
   *
   * @return
   *   a Double representing the height of the environment.
   */
  val height: Double

/**
 * Companion object for [[Environment]], providing a factory method to create instances.
 */
object Environment:
  /**
   * Creates a new instance of [[Environment]] with the specified width and height.
   *
   * @param width
   *   the width of the environment.
   * @param height
   *   the height of the environment.
   * @return
   *   a new [[Environment]] instance with the given dimensions.
   */
  def apply(width: Double, height: Double): Environment = EnvironmentImpl(width, height)

  private final case class EnvironmentImpl(width: Double, height: Double) extends Environment
