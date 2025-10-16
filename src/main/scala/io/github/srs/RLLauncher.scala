package io.github.srs

import io.github.srs.controller.RLControllerModule
import io.github.srs.controller.RLControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.BaseSimulationState
import io.github.srs.model.ModelModule
import io.github.srs.model.logic.rlLogicsBundle
import cats.effect.IO

object RLLauncher
    extends ModelModule.Interface[BaseSimulationState]
    with RLControllerModule.Interface[BaseSimulationState]:
  val model: Model[BaseSimulationState] = Model()
  val controller: Controller[BaseSimulationState] = Controller()

  def run: IO[Unit] = controller.start
