package io.github.srs

import scala.annotation.targetName
import scala.language.postfixOps

import cats.effect.IO
import io.github.srs.config.SimulationConfig
import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.logic.simulationStateLogicsBundle
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.view.ViewModule.View
import io.github.srs.view.{ CLIComponent, GUIComponent, ViewModule }

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

  /**
   * Runs the MVC components with the given initial simulation state.
   * @param state
   *   the initial state of the simulation.
   * @return
   *   an IO effect that runs the simulation.
   */
  def runMVC(state: SimulationState): IO[SimulationState] =
    for finalState <- controller.start(state)
    yield finalState

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

object Launcher:

  /**
   * Runs the simulation with the given configuration.
   * @param headless
   *   the flag indicating whether to run in headless mode (CLI) or with GUI.
   * @return
   *   an IO effect that runs the simulation and returns an optional final state.
   */
  def run(headless: Boolean = true)(simulationConfig: SimulationConfig[ValidEnvironment]): IO[Option[SimulationState]] =
    val launcher = if headless then CLILauncher else GUILauncher
    val initialState = SimulationState.from(simulationConfig, headless)
    if headless && simulationConfig.simulation.duration.isEmpty then IO.pure(None)
    else launcher.runMVC(initialState).map(Some(_))

extension (simulationConfig: SimulationConfig[ValidEnvironment])

  /**
   * Infix operator to run the simulation with the given configuration.
   * @return
   *   an IO effect that runs the simulation and returns an optional final state.
   */
  @targetName("run")
  infix def >>> : IO[Option[SimulationState]] =
    Launcher.run()(simulationConfig)
