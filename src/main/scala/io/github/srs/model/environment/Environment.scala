package io.github.srs.model.environment

import io.github.srs.model.entity.Entity
import io.github.srs.utils.SimulationDefaults.Environment.*

/**
 * Represents the environment in which entities exist.
 *
 * The environment is defined by its width and height, which are used to constrain the movement and positioning of
 * entities within it.
 */
trait EnvironmentParameters:
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

end EnvironmentParameters

/**
 * Represents an environment with a specific width, height, and a set of entities.
 */
final case class Environment(
    override val width: Int = defaultWidth,
    override val height: Int = defaultHeight,
    override val entities: Set[Entity] = defaultEntities,
) extends EnvironmentParameters

extension (env: Environment)
  /** Derives the static view of the environment. */
  def view: EnvironmentView = EnvironmentView.static(env)

object ValidEnvironment:
  opaque type ValidEnvironment = Environment

  private[environment] def from(env: Environment): ValidEnvironment =
    env

  given Conversion[ValidEnvironment, Environment] = identity

export ValidEnvironment.ValidEnvironment
