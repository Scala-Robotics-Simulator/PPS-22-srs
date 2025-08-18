package io.github.srs

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import cats.effect.IO
import io.github.srs.config.SimulationConfig
import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.model.logic.simulationStateLogicsBundle
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.SimpleRNG
import io.github.srs.view.ViewModule
import io.github.srs.view.ViewModule.View

/**
 * Launcher object that initializes the simulation.
 */
object Launcher
    extends ModelModule.Interface[SimulationState]
    with ViewModule.Interface[SimulationState]
    with ControllerModule.Interface[SimulationState]:

  val model: Model[SimulationState] = Model()
  val view: View[SimulationState] = View()
  val controller: Controller[SimulationState] = Controller()

  def runMVC(state: SimulationState): IO[Unit] =
    for
      _ <- controller.start(state)
      _ <- IO.never
    yield ()

/**
 * Creates the initial state of the simulation based on the provided configuration.
 *
 * @param cfg
 *   the simulation configuration to use for initializing the state
 * @return
 *   the initial state of the simulation
 */
def mkInitialState(cfg: SimulationConfig): SimulationState =
  val environment = cfg.environment.validate.getOrElse(
    sys.exit(1),
  )
  SimulationState(
    i = 0,
    simulationTime = cfg.simulation.duration.map(FiniteDuration(_, MILLISECONDS)),
    simulationSpeed = SimulationSpeed.NORMAL,
    simulationRNG = SimpleRNG(cfg.simulation.seed.getOrElse(42)),
    simulationStatus = SimulationStatus.RUNNING,
    environment = environment,
  )
