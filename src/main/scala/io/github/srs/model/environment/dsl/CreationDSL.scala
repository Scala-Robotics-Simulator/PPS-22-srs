package io.github.srs.model.environment.dsl

import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.staticentity.StaticEntity.Boundary
import io.github.srs.model.environment.{Environment, ValidEnvironment}
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.{bounded, noCollisions, withinBounds}

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
     * Validates the environment.
     * @return
     *   A [[Validation]] that contains the validated environment or an error message if validation fails.
     */
    infix def validate: Validation[ValidEnvironment] =
      validate(insertBoundaries = true)

    /**
     * Validates the environment with an option to insert boundaries.
     * @param insertBoundaries
     *   whether to insert boundaries into the environment.
     * @return
     *   A [[Validation]] that contains the validated environment or an error message if validation fails.
     */
    infix def validate(insertBoundaries: Boolean): Validation[ValidEnvironment] =
      import io.github.srs.utils.SimulationDefaults.Environment.*
      val boundaries = if insertBoundaries then Boundary.createBoundaries(env.width, env.height) else Set.empty[Entity]
      for
        width <- bounded("width", env.width, minWidth, maxWidth, includeMax = true)
        height <- bounded("height", env.height, minHeight, maxHeight, includeMax = true)
        _ <- bounded("entities", env.entities.size, 0, maxEntities, includeMax = true)
        entities <- withinBounds("entities", env.entities, width, height)
        entities <- noCollisions("entities", entities ++ boundaries)
      yield ValidEnvironment(width, height, entities)
  end extension
end CreationDSL
