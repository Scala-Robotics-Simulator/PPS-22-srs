package io.github.srs.model.environment

import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.staticentity.StaticEntity.Boundary
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.*

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
  val width: Int

  /**
   * The height of the environment.
   *
   * @return
   *   a Double representing the height of the environment.
   */
  val height: Int

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
  def apply(width: Int, height: Int, entities: Set[Entity] = Set.empty): Validation[Environment] =
    import io.github.srs.utils.SimulationDefaults.Environment.*
    val boundaries = Boundary.createBoundaries(width, height)
    for
      width <- bounded("width", width, minWidth, maxWidth + 1)
      height <- bounded("height", height, minHeight, maxHeight + 1)
      _ <- bounded("entities", entities.size, 0, maxEntities + 1)
      entities <- withinBounds("entities", entities, width, height)
      entities <- noCollisions("entities", entities ++ boundaries)
    yield EnvironmentImpl(width, height, entities)

  /**
   * Extracts the width, height, and entities from an [[Environment]] instance.
   * @param env
   *   the environment to extract values from.
   * @return
   *   an Option containing a tuple of (width, height, entities) if the environment is valid.
   */
  def unapply(env: Environment): Option[(Double, Double, Set[Entity])] =
    Some((env.width, env.height, env.entities))

  private final case class EnvironmentImpl(width: Int, height: Int, entities: Set[Entity]) extends Environment
end Environment

extension (env: Environment)
  /** Derives the static view of the environment. */
  def view: EnvironmentView = EnvironmentView.static(env)
