package io.github.srs.controller

import cats.effect.unsafe.implicits.global
import io.github.srs.model.ModelModule
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.RNG
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.Action
import cats.Id
import io.github.srs.model.Simulation.simulation
import io.github.srs.model.logic.RLLogicsBundle
import cats.effect.IO

object RLControllerModule:

  /**
   * Controller trait defines the interface for a Reinforcement Learning controller.
   */
  trait Controller[S <: ModelModule.BaseState]:

    /**
     * Starts the controller, performing any necessary initialization or setup.
     *
     * @return
     *   an [[cats.effect.IO]] effect representing the start operation.
     */
    def start: IO[Unit]

    /**
     * The type of the response returned after each simulation step.
     */
    type StepResponse

    /**
     * The type of the image used for rendering the simulation on the RL client.
     */
    type Image

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

  trait Provider[S <: ModelModule.BaseState]:
    val controller: Controller[S]

  type Requirements[S <: ModelModule.BaseState] = ModelModule.Provider[S]

  trait Component[S <: ModelModule.BaseState]:
    context: Requirements[S] =>

    object Controller:
      def apply()(using bundle: RLLogicsBundle[S]): Controller[S] = new ControllerImpl

      private class ControllerImpl(using bundle: RLLogicsBundle[S]) extends Controller[S]:

        type StepResponse = String
        type Image = String

        override def start: IO[Unit] =
          IO.println("Starting RL Controller") *> IO.never

        private var initialState: S =
          bundle.stateLogic.createState(SimulationConfig(simulation, ValidEnvironment.empty))

        private var state: S = initialState

        override def init(config: SimulationConfig[ValidEnvironment]): Unit =
          initialState = bundle.stateLogic.createState(config)
          state = initialState

        override def reset(rng: RNG): Unit =
          state = context.model.update(state)(using _ => bundle.stateLogic.updateState(initialState, rng))

        override def step(actions: Map[DynamicEntity, Action[Id]]): StepResponse =
          state = context.model.update(state)(using s => bundle.tickLogic.tick(s, state.dt)).unsafeRunSync()
          "Called step"

        override def render(): Image = "This is an image"
      end ControllerImpl
    end Controller
  end Component

  trait Interface[S <: ModelModule.BaseState] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end RLControllerModule
