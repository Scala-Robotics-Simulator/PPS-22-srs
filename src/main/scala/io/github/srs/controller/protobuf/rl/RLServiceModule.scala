package io.github.srs.controller.protobuf.rl

import cats.effect.IO
import io.github.srs.model.ModelModule
import io.github.srs.controller.RLControllerModule
import io.github.srs.protos.rl.*
import io.grpc.*
import com.google.protobuf.ByteString

/**
 * Module that exposes a simple RL gRPC service used by the RL controller feature.
 */
object RLServiceModule:

  /**
   * Marker trait for the RL service implementation.
   *
   * Concrete implementations must also implement the generated `RLFs2Grpc` interface for serving gRPC streams.
   */
  trait Service

  /**
   * Provider for the RL service.
   *
   * Exposes a `service` value which is both a lightweight `Service` and the generated `RLFs2Grpc[IO, Metadata]` gRPC
   * service used by the server wiring.
   */
  trait Provider:
    val service: Service & RLFs2Grpc[IO, Metadata]

  /**
   * Requirements for components that need the RL service.
   *
   * This type alias expresses that components requiring the RL service must also provide the
   * `RLControllerModule.Provider` for access to controller state / context.
   */
  type Requirements[S <: ModelModule.BaseState] = RLControllerModule.Provider[S]

  /**
   * Component that builds the RL gRPC service implementation.
   *
   * The component depends on `Requirements` so it can access controller state when a richer implementation is wired in.
   * The default `Service` simply pattern matches incoming `RLRequest` messages and returns static / empty responses. It
   * is suitable for local testing and integration.
   *
   * @tparam S
   *   the concrete simulation state type used by the controller wiring.
   */
  trait Component[S <: ModelModule.BaseState]:
    context: Requirements[S] =>

    object Service:

      /**
       * Creates the default RL service implementation.
       *
       * @return
       *   an object that is both the module's `Service` marker and the generated `RLFs2Grpc[IO, Metadata]`
       *   implementation.
       */
      def apply(): Service & RLFs2Grpc[IO, Metadata] = new ServiceImpl

      /**
       * Private, minimal service implementation.
       *
       *   - `interact` accepts a stream of `RLRequest` and maps each request to an `RLResponse`. The current responses
       *     are empty placeholders. A production implementation should inspect the request and interact with the
       *     controller/state accordingly.
       */
      private class ServiceImpl extends Service with RLFs2Grpc[IO, Metadata]:

        /**
         * Handle a streaming RL interaction.
         *
         * The method transforms the incoming stream of `RLRequest` messages into a stream of `RLResponse`. Supported
         * request kinds (Init, Reset, Step, Close) produce simple placeholder responses. Any unknown request produces
         * an Error response.
         *
         * @param request
         *   stream of incoming RL requests
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   stream of RL responses corresponding to each request
         */
        override def interact(
            request: fs2.Stream[IO, RLRequest],
            ctx: Metadata,
        ): fs2.Stream[IO, RLResponse] =
          request.map:
            case RLRequest(RLRequest.Request.Init(InitRequest(config, _)), _) =>
              RLResponse(
                response = RLResponse.Response.Init(
                  InitResponse(ok = true, message = None),
                ),
              )

            case RLRequest(RLRequest.Request.Reset(_), _) =>
              RLResponse(
                response = RLResponse.Response.Reset(
                  ResetResponse(observations = Map.empty, info = Map.empty),
                ),
              )

            case RLRequest(RLRequest.Request.Step(_), _) =>
              RLResponse(
                response = RLResponse.Response.Step(
                  StepResponse(
                    observations = Map.empty,
                    rewards = Map.empty,
                    terminateds = Map.empty,
                    truncateds = Map.empty,
                    infos = Map.empty,
                  ),
                ),
              )

            case RLRequest(RLRequest.Request.Render(RenderRequest(w, h, _)), _) =>
              val width = w.getOrElse(800)
              val height = h.getOrElse(600)
              RLResponse(
                response = RLResponse.Response.Render(
                  RenderResponse(
                    image = ByteString.EMPTY,
                    format = "png",
                    width = width,
                    height = height,
                    channels = 3,
                  ),
                ),
              )

            case RLRequest(RLRequest.Request.Close(_), _) =>
              RLResponse(
                response = RLResponse.Response.Close(
                  CloseResponse(ok = true, message = None),
                ),
              )

            case _ =>
              RLResponse(
                response = RLResponse.Response.Error(
                  ErrorResponse(message = "Unknown request", code = ErrorCode.UNKNOWN),
                ),
              )

      end ServiceImpl

    end Service

  end Component

  /**
   * Combined provider + component interface for consumers of the RL service.
   */
  trait Interface[S <: ModelModule.BaseState] extends Provider with Component[S]:
    self: Requirements[S] =>
end RLServiceModule
