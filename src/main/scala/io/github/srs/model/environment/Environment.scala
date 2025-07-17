package io.github.srs.model.environment

import io.github.srs.model.Entity
import io.github.srs.model.validation.Validation.*
import io.github.srs.model.validation.Validation

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
   * A set of entities that exist within the environment.
   *
   * @return
   *   a Set of [[Entity]] representing the entities in the environment.
   */
  val entities: Set[Entity]

end Environment

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
  def apply(width: Double, height: Double, entities: Set[Entity] = Set.empty): Validation[Environment] =
    for
      width <- positive("width", width)
      height <- positive("height", height)
    yield EnvironmentImpl(width, height, entities)

  /**
   * Extracts the width, height, and entities from an [[Environment]] instance.
   * @param env
   *   the environment to extract values from.
   * @return
   *   a tuple containing the width, height, and a set of entities in the environment.
   */
  def unapply(env: Environment): Environment =
    EnvironmentImpl(env.width, env.height, env.entities)

  private final case class EnvironmentImpl(width: Double, height: Double, entities: Set[Entity]) extends Environment
end Environment
