package io.github.srs.controller.protobuf.rl

import scala.annotation.unused

import cats.effect.unsafe.implicits.global
import cats.effect.IO
import io.github.srs.model.ModelModule
import io.github.srs.controller.RLControllerModule
import io.github.srs.protos.rl.*
import io.grpc.*
import com.google.protobuf.ByteString
import io.github.srs.config.yaml.YamlManager
import io.github.srs.model.environment.dsl.CreationDSL.validate
import io.github.srs.utils.random.SimpleRNG

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
                  manageInitRequest(config),
                ),
              )

            case RLRequest(RLRequest.Request.Reset(ResetRequest(seed, options, _)), _) =>
              RLResponse(
                response = RLResponse.Response.Reset(
                  manageResetRequest(seed, options),
                ),
              )

            case RLRequest(RLRequest.Request.Step(StepRequest(actions, _)), _) =>
              RLResponse(
                response = RLResponse.Response.Step(
                  manageStepRequest(actions),
                ),
              )

            case RLRequest(RLRequest.Request.Render(RenderRequest(w, h, _)), _) =>
              val width = w.getOrElse(800)
              val height = h.getOrElse(600)
              RLResponse(
                response = RLResponse.Response.Render(
                  manageRenderRequest(width, height),
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

        private def manageInitRequest(@unused config: String): InitResponse =
          val simulationConfig = YamlManager.parse[IO](config).unsafeRunSync()
          simulationConfig match
            case Left(errors) => InitResponse(ok = false, message = Some(errors.mkString("\n")))
            case Right(config) =>
              config.environment.validate match
                case Left(error) => InitResponse(ok = false, message = Some(error.toString))
                case Right(env) =>
                  context.controller.init(config.simulation in env)
                  InitResponse(ok = true, message = None)

        private def manageResetRequest(seed: Option[Int], @unused options: Map[String, String]): ResetResponse =
          import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
          val rng = seed match
            case Some(seed) => SimpleRNG(seed)
            case None => context.controller.initialState.simulationRNG
          val (obs, deInfos) = context.controller.reset(rng)
          val observations = obs.map { (ent, readings) =>
            ent.id.toString -> Observation(
              proximityValues = readings.proximityReadings.map(_.value),
              lightValues = readings.lightReadings.map(_.value),
            )
          }
          val infos = deInfos.map { (ent, info) => ent.id.toString -> info }

          ResetResponse(observations = observations, info = infos)

        private def manageStepRequest(@unused actions: Map[String, ContinuousAction]): StepResponse =
          StepResponse(
            observations = Map.empty,
            rewards = Map.empty,
            terminateds = Map.empty,
            truncateds = Map.empty,
            infos = Map.empty,
          )

        private def manageRenderRequest(width: Int, height: Int): RenderResponse =
          RenderResponse(
            image = ByteString.EMPTY,
            format = "png",
            width = width,
            height = height,
            channels = 3,
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
