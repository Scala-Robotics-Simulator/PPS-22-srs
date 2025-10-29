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

/** Factory object for creating RLLauncher instances */
object RLLauncher:
  /**
   * Creates a new RLLauncher with the specified port
   * @param port
   *   the port number for the gRPC server (default: 50051)
   * @return
   *   a new RLLauncher instance
   */
  def apply(port: Int = 50051): RLLauncher = new RLLauncher(port)

/**
 * RL Launcher that starts a gRPC server for reinforcement learning
 * @param port
 *   the port number for the gRPC server
 */
class RLLauncher(port: Int)
    extends ModelModule.Interface[BaseSimulationState]
    with RLControllerModule.Interface[BaseSimulationState]
    with RLServiceModule.Interface[BaseSimulationState]
    with RLServerModule.Interface[BaseSimulationState]:
  val model = Model()
  val controller = Controller()
  val service = Service()
  val server = Server(port)

  def run: IO[Unit] = IO.println(s"Starting rl-server on port $port") *> server.run
