package io.github.srs.controller

import cats.effect.unsafe.implicits.global
import io.github.srs.model.ModelModule
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.utils.random.RNG
import io.github.srs.model.entity.dynamicentity.action.Action
import cats.Id
import io.github.srs.model.Simulation.simulation
import io.github.srs.model.logic.RLLogicsBundle
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.view.rendering.EnvironmentRenderer
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.controller.message.DynamicEntityProposal
import cats.effect.IO
import io.github.srs.utils.random.SimpleRNG

object RLControllerModule:

  type Observations = Map[Agent, SensorReadings]
  type Infos = Map[Agent, String]

  /**
   * The resonse after each simulation step.
   */
  case class StepResponse private[RLControllerModule] (
      observations: Observations,
      rewards: Map[Agent, Double],
      terminateds: Map[Agent, Boolean],
      truncateds: Map[Agent, Boolean],
      infos: Infos,
  )

  /**
   * Controller trait defines the interface for a Reinforcement Learning controller.
   */
  trait Controller[S <: ModelModule.BaseState]:

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
    def step(actions: Map[Agent, Action[IO]]): StepResponse

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
          (state.environment.createObservations, state.environment.createInfos)

        override def step(actions: Map[Agent, Action[IO]]): StepResponse =
          _state = context.model.update(state)(using s => bundle.tickLogic.tick(s, state.dt)).unsafeRunSync()
          val actionsList =
            actions.map { (agent, action) => DynamicEntityProposal(agent, action) }.toList.sortBy(_.entity.id)
          _state = context.model
            .update(state)(using
              s => bundle.dynamicEntityActionsLogic.handleDynamicEntityActionsProposals(s, actionsList),
            )
            .unsafeRunSync()
          _state = context.model
            .update(state)(using s => bundle.randomLogic.random(s, SimpleRNG(s.simulationRNG.nextLong._1)))
            .unsafeRunSync()
          StepResponse(
            observations = state.environment.createObservations,
            rewards = Map.empty,
            terminateds = Map.empty,
            truncateds = Map.empty,
            infos = state.environment.createInfos,
          )

        override def render(width: Int, height: Int): Image =
          EnvironmentRenderer.renderToPNG(state.environment, width, height)
      end ControllerImpl
    end Controller

    extension (env: ValidEnvironment)

      def createObservations: Observations =
        env.entities.collect { case a: Agent =>
          a -> a.senseAll[Id](env)
        }.toMap

      def createInfos: Infos =
        env.entities.collect { case a: Agent =>
          a -> ""
        }.toMap
  end Component

  trait Interface[S <: ModelModule.BaseState] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end RLControllerModule
