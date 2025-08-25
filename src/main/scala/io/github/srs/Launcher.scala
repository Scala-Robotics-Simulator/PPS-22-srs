package io.github.srs

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.effect.IO
import io.github.srs.config.SimulationConfig
import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.environment.ValidEnvironment
import io.github.srs.model.logic.simulationStateLogicsBundle
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.SimpleRNG
import io.github.srs.view.{ CLIComponent, GUIComponent, ViewModule }
import io.github.srs.view.ViewModule.View

/**
 * Base trait for launching the Scala Robotics Simulator application.
 *
 * It sets up the Model-View-Controller (MVC) architecture and provides a method to run the simulation.
 *
 * The specific view implementation (GUI or CLI) is provided by the extending objects.
 */
trait BaseLauncher
    extends ModelModule.Interface[SimulationState]
    with ControllerModule.Interface[SimulationState]
    with ViewModule.Interface[SimulationState]:

  val model: Model[SimulationState] = Model()
  val controller: Controller[SimulationState] = Controller()
  val view: View[SimulationState]

  def runMVC(state: SimulationState): IO[Unit] =
    for _ <- controller.start(state)
    yield ()

/**
 * Launcher object for the GUI version of the Scala Robotics Simulator.
 */
object GUILauncher extends BaseLauncher with GUIComponent[SimulationState]:
  override val view: View[SimulationState] = View()

/**
 * Launcher object for the CLI version of the Scala Robotics Simulator.
 */
object CLILauncher extends BaseLauncher with CLIComponent[SimulationState]:
  override val view: View[SimulationState] = View()

/**
 * Creates the initial state of the simulation based on the provided configuration.
 *
 * @param cfg
 *   the simulation configuration to use for initializing the state
 * @return
 *   the initial state of the simulation
 */
def mkInitialState(cfg: SimulationConfig[ValidEnvironment]): SimulationState =
  SimulationState(
    simulationTime = cfg.simulation.duration.map(FiniteDuration(_, MILLISECONDS)),
    simulationSpeed = SimulationSpeed.NORMAL,
    simulationRNG = SimpleRNG(cfg.simulation.seed.getOrElse(42)),
    simulationStatus = SimulationStatus.PAUSED,
    environment = cfg.environment,
  )
