package io.github.srs

import io.github.srs.controller.ControllerModule
import io.github.srs.controller.ControllerModule.Controller
import io.github.srs.model.ModelModule
import io.github.srs.model.ModelModule.Model
import io.github.srs.view.ViewModule
import io.github.srs.view.ViewModule.View

object Launcher extends ModelModule.Interface with ViewModule.Interface with ControllerModule.Interface:

  val model: Model = Model()
  val view: View = View()
  val controller: Controller = Controller()

  @main def run(): Unit =
    controller.start()
