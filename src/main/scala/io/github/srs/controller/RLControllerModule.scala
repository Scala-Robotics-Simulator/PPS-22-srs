package io.github.srs.controller

import io.github.srs.model.ModelModule
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.RNG
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.Action
import cats.Id

object RLControllerModule:

  /**
   * Controller trait defines the interface for a Reinforcement Learning controller.
   */
  trait Controller[S <: ModelModule.BaseState]:
    /**
     * The type of the response returned after each simulation step.
     */
    type StepResponse

    /**
     * The type of the image used for rendering the simulation on the RL client.
     */
    type Image

    /**
     * The initial state of the simulation, if available.
     */
    def initialState: Option[S]

    /**
     * The current state of the simulation, if available.
     */
    def state: Option[S]

    /**
     * Initializes the controller with the given simulation configuration.
     *
     * @param config
     *   the simulation configuration to initialize the controller.
     */
    def init(config: SimulationConfig[ValidEnvironment]): Unit

    /**
     * Resets the controller to its initial state using the provided random number generator.
     *
     * @param rng
     *   the random number generator to use for reproducibility in the next run.
     */
    def reset(rng: RNG): Unit

    /**
     * Performs a simulation step using the provided actions for each agent.
     *
     * @param actions
     *   a map of agents to their corresponding actions to be performed in this step.
     * @return
     *   a response containing the results of the simulation step.
     */
    def step(actions: Map[DynamicEntity, Action[Id]]): StepResponse

    /**
     * Renders the current state of the simulation to an image for the RL client.
     *
     * @return
     *   an image representing the current state of the simulation.
     */
    def render(): Image
  end Controller
end RLControllerModule
