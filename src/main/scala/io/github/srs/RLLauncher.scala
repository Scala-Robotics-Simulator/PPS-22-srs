package io.github.srs

import io.github.srs.controller.RLControllerModule
import io.github.srs.controller.RLControllerModule.Controller
import io.github.srs.model.ModelModule.Model
import io.github.srs.model.BaseSimulationState
import io.github.srs.model.ModelModule
import io.github.srs.model.logic.rlLogicsBundle
import cats.effect.IO
import io.github.srs.controller.protobuf.rl.RLServiceModule
import io.github.srs.controller.protobuf.rl.RLServerModule
import io.github.srs.controller.protobuf.rl.RLServerModule.Server

object RLLauncher
    extends ModelModule.Interface[BaseSimulationState]
    with RLControllerModule.Interface[BaseSimulationState]
    with RLServiceModule.Interface[BaseSimulationState]
    with RLServerModule.Interface[BaseSimulationState]:
  val model = Model()
  val controller = Controller()
  val service = Service()
  val server = Server(50051)

  def run: IO[Unit] = IO.println("Starting rl-server") *> server.run
