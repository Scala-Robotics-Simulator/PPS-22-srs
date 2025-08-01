package io.github.srs

import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.SimulationState
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
    controller.start(SimulationState(0)).runAsyncAndForget
