package io.github.srs

import scala.concurrent.duration.DurationInt

import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.logic.IncreaseLogic.given
import io.github.srs.model.logic.TimeLogic.given
import io.github.srs.model.{ ModelModule, SimulationState }
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
    controller.start(SimulationState(0, 10000.millis)).runAsyncAndForget
