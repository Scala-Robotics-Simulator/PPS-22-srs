package io.github.srs.controller

import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.controller.RLControllerModule
import io.github.srs.controller.RLControllerModule.Controller
import io.github.srs.model.BaseSimulationState
import io.github.srs.model.logic.rlLogicsBundle
import io.github.srs.model.Simulation.*
import io.github.srs.model.dsl.Cell.*
import io.github.srs.model.dsl.GridDSL.{ *, given }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.environment.dsl.CreationDSL.*
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import io.github.srs.utils.random.SimpleRNG
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory
import cats.effect.IO

class RLControllerTest
    extends AnyFlatSpec
    with BeforeAndAfterEach
    with Matchers
    with ModelModule.Interface[BaseSimulationState]
    with RLControllerModule.Interface[BaseSimulationState]:
  val model = Model()
  val controller = Controller()

  private val config = simulation withDuration 1000 withSeed 42 in
    (-- | -- | -- | -- | -- ||
      AG | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | -- ||
      -- | -- | -- | -- | --).validate.toOption.value

  override protected def beforeEach(): Unit =
    controller.init(config)

  "RLController" should "correctly load a configuration" in:
    val env: Environment =
      -- | -- | -- | -- | -- ||
        AG | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | -- ||
        -- | -- | -- | -- | --
    val prevInitialState = controller.initialState
    val prevState = controller.state

    val valEnv: ValidEnvironment = env.validate.toOption.value
    val duration = 100_000
    val config = simulation withDuration duration withSeed 42 in valEnv
    controller.init(config)
    val _ = controller.initialState should not equal (prevInitialState)
    val _ = controller.state should not equal (prevState)

  "RLController" should "correcly reset the simulation" in:
    val _ = controller.step(Map.empty)
    val state = controller.state
    val (_, _) = controller.reset(rng = SimpleRNG(420))
    val _ = controller.state.elapsedTime should not equal (state.elapsedTime)
    controller.state.simulationRNG should not equal (controller.initialState.simulationRNG)

  "RLController" should "correcly update when calling step" in:
    val agent = controller.state.environment.entities.collectFirst { case a: Agent => a }.value
    val initialRng = controller.state.simulationRNG
    val _ = controller.state.elapsedTime should equal(controller.initialState.elapsedTime)
    val _ = controller.step(Map(agent -> MovementActionFactory.moveForward[IO]))
    val updatedAgent = controller.state.environment.entities.collectFirst { case a: Agent => a }.value
    val _ = updatedAgent.id should equal(agent.id)
    val _ = updatedAgent.position.x should be > agent.position.x
    val _ = updatedAgent.position.y should equal(agent.position.y)
    val _ = controller.state.simulationRNG should not equal (initialRng)
    controller.state.elapsedTime should not equal (controller.initialState.elapsedTime)
end RLControllerTest
