package io.github.srs.model.environment.dsl

import cats.syntax.all.*
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.staticentity.StaticEntity.Boundary
import io.github.srs.model.environment.{ Environment, ValidEnvironment }
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.{ bounded, noCollisions, withinBounds }
import io.github.srs.utils.SimulationDefaults.Environment.*
import io.github.srs.model.entity.Entity.validateEntity
import io.github.srs.utils.SimulationDefaults.LightMapConfigs
import io.github.srs.utils.SimulationDefaults.Fields.Environment.Self

/**
 * The DSL for creating an environment in the simulation.
 */
object CreationDSL:

  /**
   * Creates a new environment with default properties.
   * @return
   *   A new instance of [[Environment]] with default values.
   */
  def environment: Environment = Environment()

  /**
   * Provides an extension method for the Environment class to allow for a more fluent DSL.
   */
  extension (env: Environment)

    /**
     * Sets the width of the environment.
     * @param width
     *   the width of the environment.
     * @return
     *   The updated environment with the specified width.
     */
    infix def withWidth(width: Int): Environment = env.copy(width = width)

    /**
     * Use high-precision lighting (scale factor 100)
     */
    def withHighPrecisionLighting: Environment =
      env.copy(_lightMap = Some(LightMapConfigs.HPLightMap))

    /**
     * Use fast lighting (scale factor 5)
     */
    def withFastLighting: Environment =
      env.copy(_lightMap = Some(LightMapConfigs.fastLightMap))

    /**
     * Use simple lighting without caching
     */
    def withDefaultLighting: Environment =
      env.copy(_lightMap = Some(LightMapConfigs.BaseLightMap))

    /**
     * Sets the height of the environment.
     * @param height
     *   the height of the environment.
     * @return
     *   The updated environment with the specified height.
     */
    infix def withHeight(height: Int): Environment = env.copy(height = height)

    /**
     * Adds a set of entities to the environment. This method allows for adding multiple entities at once.
     * @param entities
     *   a set of entities to add to the environment.
     * @return
     *   The updated environment with the new entities added.
     */
    infix def containing(entities: Set[Entity]): Environment =
      env.copy(entities = env.entities ++ entities)

    /**
     * Adds an entity to the environment.
     * @param entity
     *   the entity to add to the environment.
     * @return
     *   The updated environment with the new entity added.
     */
    infix def containing(entity: Entity): Environment =
      env.copy(entities = env.entities + entity)

    /**
     * Adds an entity to the environment using infix notation.
     * @param entity
     *   the entity to add to the environment.
     * @return
     *   The updated environment with the new entity added.
     */
    infix def and(entity: Entity): Environment =
      env.copy(entities = env.entities + entity)

    /**
     * Validates the environment with an option to insert boundaries.
     * @return
     *   A [[io.github.srs.model.validation.Validation]] that contains the validated environment or an error message if
     *   validation fails.
     */
    infix def validate: Validation[ValidEnvironment] =
      val entities = env.entities.filterNot:
        case _: Boundary => true
        case _ => false
      val boundaries = Boundary.createBoundaries(env.width, env.height)
      for
        width <- bounded(s"$Self width", env.width, MinWidth, MaxWidth, includeMax = true)
        height <- bounded(s"$Self height", env.height, MinHeight, MaxHeight, includeMax = true)
        _ <- bounded(s"$Self entities", env.entities.size, 0, MaxEntities, includeMax = true)
        entities <- withinBounds(s"$Self entities", entities, width, height)
        entities <- noCollisions(s"$Self entities", entities ++ boundaries)
        _ <- entities.toList.traverse_(validateEntity)
      yield ValidEnvironment.from(env.copy(entities = entities))
  end extension
end CreationDSL
