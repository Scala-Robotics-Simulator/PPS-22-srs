package io.github.srs.controller

import cats.Id
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.config.SimulationConfig
import io.github.srs.controller.message.DynamicEntityProposal
import io.github.srs.logger
import io.github.srs.model.ModelModule
import io.github.srs.model.Simulation.simulation
import io.github.srs.model.entity.dynamicentity.action.{ Action, NoAction }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.model.logic.RLLogicsBundle
import io.github.srs.utils.random.{ RNG, SimpleRNG }
import io.github.srs.view.rendering.EnvironmentRenderer
import io.github.srs.model.entity.Point2D.*

object RLControllerModule:

  final case class AgentObservation(
      sensorReadings: SensorReadings,
      position: (Double, Double),
      orientation: Double,
  )

  type Observations = Map[Agent, AgentObservation]
  type Infos = Map[Agent, String]
  type Rewards = Map[Agent, Double]
  type Terminateds = Map[Agent, Boolean]
  type Truncateds = Map[Agent, Boolean]

  /**
   * The resonse after each simulation step.
   */
  case class StepResponse private[RLControllerModule] (
      observations: Observations,
      rewards: Rewards,
      terminateds: Terminateds,
      truncateds: Truncateds,
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
          logger.debug(s"new config, state = \n $state")

        override def reset(rng: RNG): (Observations, Infos) =
          _state = context.model.update(state)(using _ => bundle.stateLogic.updateState(initialState, rng))
          logger.debug("resetting controller")
          (state.environment.getObservations, state.environment.getInfos)

        override def step(actions: Map[Agent, Action[IO]]): StepResponse =
          logger.debug("sending step command to controller")
          logger.debug(s"actions: $actions")
          _state = context.model.update(state)(using s => bundle.tickLogic.tick(s, state.dt)).unsafeRunSync()
          val actionsList =
            actions.map { (agent, action) =>
              DynamicEntityProposal(agent.copy(lastAction = Some(action), aliveSteps = agent.aliveSteps + 1), action)
            }.toList.sortBy(_.entity.id)
          val prevState = state
          _state = context.model
            .update(state)(using
              s => bundle.dynamicEntityActionsLogic.handleDynamicEntityActionsProposals(s, actionsList),
            )
            .unsafeRunSync()
          _state = context.model
            .update(state)(using s => bundle.randomLogic.random(s, SimpleRNG(s.simulationRNG.nextLong._1)))
            .unsafeRunSync()
          val observations = state.environment.getObservations
          val rewards = state.getRewards(prevState)
          val terminateds = state.getTerminations(prevState)
          val truncateds = state.getTruncations(prevState)
          val infos = state.environment.getInfos
          logger.debug(s"step completed, state = \n $state")
          logger.debug(s"observations: $observations")
          logger.debug(s"rewards: $rewards")
          logger.debug(s"terminateds: $terminateds")
          logger.debug(s"truncateds: $truncateds")
          logger.debug(s"infos: $infos")
          StepResponse(
            observations,
            rewards,
            terminateds,
            truncateds,
            infos,
          )

        end step

        override def render(width: Int, height: Int): Image =
          EnvironmentRenderer.renderToPNG(state.environment, width, height)
      end ControllerImpl
    end Controller

    extension (s: ModelModule.BaseState)

      def getTerminations(prev: ModelModule.BaseState): Terminateds =
        s.environment.entities.collect { case a: Agent =>
          a -> a.termination.evaluate(prev, s, a, a.lastAction.getOrElse(NoAction[IO]()))
        }.toMap

      def getTruncations(prev: ModelModule.BaseState): Truncateds =
        s.environment.entities.collect { case a: Agent =>
          a -> a.truncation.evaluate(prev, s, a, a.lastAction.getOrElse(NoAction[IO]()))
        }.toMap

      def getRewards(prev: ModelModule.BaseState): Rewards =
        s.environment.entities.collect { case a: Agent =>
          a -> a.reward.evaluate(prev, s, a, a.lastAction.getOrElse(NoAction[IO]()))
        }.toMap

    extension (env: ValidEnvironment)

      def getObservations: Observations =
        env.entities.collect { case a: Agent =>
          val sensors = a.senseAll[Id](env)
          val position = (a.position.x, a.position.y)
          val orientation = a.orientation.degrees
          a -> AgentObservation(sensors, position, orientation)
        }.toMap

      def getInfos: Infos =
        env.entities.collect { case a: Agent =>
          a -> ""
        }.toMap
  end Component

  trait Interface[S <: ModelModule.BaseState] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end RLControllerModule
