package io.github.srs.controller.protobuf.rl

import cats.effect.IO
import io.github.srs.model.ModelModule
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.github.srs.protos.rl.RLFs2Grpc
import com.typesafe.scalalogging.Logger

/**
 * Small module that runs a gRPC server for the RL service.
 */
object RLServerModule:

  /**
   * Represents a server that can be started. The `run` method returns an IO that never completes when the server is
   * running (conventional for long- lived servers in effectful applications).
   */
  trait Server:
    def run: IO[Unit]

  /**
   * Provider exposing a ready-to-run `Server` instance.
   */
  trait Provider:
    val server: Server

  /**
   * This module depends on an RL service provider so it can bind the generated gRPC implementation into the running
   * server.
   */
  type Requirements[S <: ModelModule.BaseState] = RLServiceModule.Provider

  /**
   * Component that constructs a `Server` given a port. The implementation builds a Netty server and binds the provided
   * RL service.
   */
  trait Component[S <: ModelModule.BaseState]:
    context: Requirements[S] =>

    object Server:
      /**
       * Create a server that listens on the provided port.
       *
       * @param port
       *   port to bind the server to
       * @return
       *   a `Server` instance
       */
      def apply(port: Int): Server = new ServerImpl(port)

      private class ServerImpl(port: Int) extends Server:
        private val logger = Logger(getClass.getName)

        /**
         * Run the server. The returned IO will start the Netty server and never complete (IO.never) while the server is
         * active. The service is obtained from the `context` (the RLService provider).
         */
        override def run: IO[Unit] =
          for
            service <- RLFs2Grpc.bindServiceResource(context.service).allocated
            (svc, _) = service
            server = NettyServerBuilder.forPort(port).addService(svc).build()
            _ <- IO(logger.debug("Starting RL server")) *> IO(server.start()) *> IO.never
          yield ()

    end Server

  end Component

  trait Interface[S <: ModelModule.BaseState] extends Provider with Component[S]:
    self: Requirements[S] =>

end RLServerModule
