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
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.view.rendering.EnvironmentRenderer

object RLControllerModule:

  type Observations = Map[DynamicEntity, SensorReadings]
  type Infos = Map[DynamicEntity, String]

  /**
   * The resonse after each simulation step.
   */
  case class StepResponse private (
      observations: Observations,
      rewards: Map[DynamicEntity, Double],
      terminateds: Map[DynamicEntity, Boolean],
      truncateds: Map[DynamicEntity, Boolean],
      infos: Infos,
  )

  /**
   * Controller trait defines the interface for a Reinforcement Learning controller.
   */
  trait Controller[S <: ModelModule.BaseState]:

    type StepResponse

    /**
     * The type of the image used for rendering the simulation on the RL client.
     */
    type Image = Array[Byte]

    /**
     * The initial state of the controller.
     */
    def initialState: S

    /**
     * The current state of the controller.
     */
    def state: S

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
    def reset(rng: RNG): (Observations, Infos)

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
     * @param width
     *   the width of the rendered image.
     * @param height
     *   the height of the rendered image.
     *
     * @return
     *   an image representing the current state of the simulation.
     */
    def render(width: Int, height: Int): Image
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

        var _initialState: S =
          bundle.stateLogic.createState(SimulationConfig(simulation, ValidEnvironment.empty))

        var _state: S = initialState

        override def initialState: S = _initialState

        override def state: S = _state

        override def init(config: SimulationConfig[ValidEnvironment]): Unit =
          _initialState = bundle.stateLogic.createState(config)
          _state = _initialState

        override def reset(rng: RNG): (Observations, Infos) =
          _state = context.model.update(state)(using _ => bundle.stateLogic.updateState(initialState, rng))
          state.environment.entities.collect {
            // TODO: will be agent
            case r: Robot => (r, r.senseAll[Id](state.environment), "")
          }.map { case (robot, readings, info) =>
            (robot -> readings, robot -> info)
          }.unzip match
            case (obsList, infosList) => (obsList.toMap, infosList.toMap)

        override def step(actions: Map[DynamicEntity, Action[Id]]): StepResponse =
          _state = context.model.update(state)(using s => bundle.tickLogic.tick(s, state.dt)).unsafeRunSync()
          "Called step"

        override def render(width: Int, height: Int): Image =
          EnvironmentRenderer.renderToPNG(state.environment, width, height)
      end ControllerImpl
    end Controller
  end Component

  trait Interface[S <: ModelModule.BaseState] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end RLControllerModule
