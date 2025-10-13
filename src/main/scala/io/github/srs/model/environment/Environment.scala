package io.github.srs.model.environment

import cats.effect.IO
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.utils.SimulationDefaults.Environment.*
import io.github.srs.model.illumination.model.LightField
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.staticentity.StaticEntity.Light
import io.github.srs.model.illumination.LightMap
import io.github.srs.utils.SimulationDefaults.LightMapConfigs

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
  def width: Int

  /**
   * The height of the environment.
   *
   * @return
   *   a Double representing the height of the environment.
   */
  def height: Int

  /**
   * A list of entities that exist within the environment.
   *
   * @return
   *   a List of [[io.github.srs.model.entity.Entity]] representing the entities in the environment.
   */
  def entities: List[Entity]

  /**
   * The light field of the environment, representing the illumination conditions.
   *
   * @return
   *   a [[io.github.srs.model.illumination.model.LightField]] representing the light field of the environment.
   */
  def lightField: LightField

end EnvironmentParameters

/**
 * Represents an environment with a specific width, height, and a list of entities.
 */
final case class Environment(
    override val width: Int = DefaultWidth,
    override val height: Int = DefaultHeight,
    override val entities: List[Entity] = DefaultEntities,
    private[environment] val _lightMap: Option[LightMap[IO]] = None,
) extends EnvironmentParameters:

  private val lightMap: LightMap[IO] = _lightMap.getOrElse(LightMapConfigs.BaseLightMap)

  lazy val lightField: LightField =
    lightMap.computeField(this, includeDynamic = true).unsafeRunSync()

object ValidEnvironment:
  opaque type ValidEnvironment <: EnvironmentParameters = Environment

  private[environment] def from(env: Environment): ValidEnvironment =
    env

  given Conversion[ValidEnvironment, Environment] = identity

export ValidEnvironment.ValidEnvironment

extension (env: Environment)

  /**
   * A list of static entities in the environment.
   * @return
   *   A list of robots
   */
  def robots: List[Robot] = env.entities.collect { case r: Robot => r }

  /**
   * A list of light entities in the environment.
   * @return
   *   A list of lights
   */
  def lights: List[Light] = env.entities.collect { case l: Light => l }
