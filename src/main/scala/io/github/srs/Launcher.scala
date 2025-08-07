package io.github.srs

//import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationConfig.{ SimulationSpeed, SimulationStatus }
import io.github.srs.model.logic.IncreaseLogic.given
import io.github.srs.model.logic.StatusLogic.given
import io.github.srs.model.logic.TimeLogic.given
import io.github.srs.model.{ ModelModule, SimulationState }
import io.github.srs.utils.random.SimpleRNG
import io.github.srs.view.ViewModule
import io.github.srs.view.ViewModule.View
import monix.execution.Scheduler.Implicits.global

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

  @main def run(): Unit =
    controller
      .start(
        SimulationState(
          i = 0,
          simulationTime = None, // Some(FiniteDuration(10_000, MILLISECONDS)),
          simulationSpeed = SimulationSpeed.NORMAL,
          simulationRNG = SimpleRNG(42L),
          simulationStatus = SimulationStatus.RUNNING,
        ),
      )
      .runAsyncAndForget
end Launcher
