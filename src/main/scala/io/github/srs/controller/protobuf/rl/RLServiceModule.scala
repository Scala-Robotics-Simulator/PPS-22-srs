package io.github.srs.controller.protobuf.rl

import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.*
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
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory
import io.github.srs.model.entity.dynamicentity.agent.Agent

/**
 * Module that exposes a simple RL gRPC service used by the RL controller feature.
 */
object RLServiceModule:

  /**
   * Marker trait for the RL service implementation.
   *
   * Concrete implementations must also implement the generated `RLFs2Grpc` interface for serving gRPC.
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
   * The service implements separate RPC methods for each RL operation (init, reset, step, render, close).
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
       * Private service implementation.
       *
       * Implements each RPC method separately:
       *   - `init` initializes the simulation with a YAML config
       *   - `reset` resets the environment with an optional seed
       *   - `step` executes actions and returns observations, rewards, etc.
       *   - `render` generates a rendered image of the environment
       *   - `close` cleans up resources
       */
      private class ServiceImpl extends Service with RLFs2Grpc[IO, Metadata]:

        /**
         * Initialize the simulation environment.
         *
         * @param request
         *   contains the YAML configuration string
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   response indicating success or failure with optional error message
         */
        override def init(request: InitRequest, ctx: Metadata): IO[InitResponse] =
          IO(manageInitRequest(request.config))

        /**
         * Reset the environment to initial state.
         *
         * @param request
         *   contains optional seed and options
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   response with observations and info for all agents
         */
        override def reset(request: ResetRequest, ctx: Metadata): IO[ResetResponse] =
          IO(manageResetRequest(request.seed))

        /**
         * Execute a step in the environment.
         *
         * @param request
         *   contains actions for each agent
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   response with observations, rewards, terminateds, truncateds, and infos
         */
        override def step(request: StepRequest, ctx: Metadata): IO[StepResponse] =
          IO(manageStepRequest(request.actions))

        /**
         * Render the current environment state.
         *
         * @param request
         *   contains optional width and height
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   response with rendered image as bytes
         */
        override def render(request: RenderRequest, ctx: Metadata): IO[RenderResponse] =
          val width = request.width.getOrElse(800)
          val height = request.height.getOrElse(600)
          IO(manageRenderRequest(width, height))

        /**
         * Close and cleanup the environment.
         *
         * @param request
         *   empty close request
         * @param ctx
         *   gRPC metadata for the call
         * @return
         *   response indicating success
         */
        override def close(request: CloseRequest, ctx: Metadata): IO[CloseResponse] =
          IO(CloseResponse(ok = true, message = None))

        private def manageInitRequest(config: String): InitResponse =
          val simulationConfig = YamlManager.parse[IO](config).unsafeRunSync()
          simulationConfig match
            case Left(errors) => InitResponse(ok = false, message = Some(errors.mkString("\n")))
            case Right(config) =>
              config.environment.validate match
                case Left(error) => InitResponse(ok = false, message = Some(error.toString))
                case Right(env) =>
                  context.controller.init(config.simulation in env)
                  InitResponse(ok = true, message = None)

        private def manageResetRequest(seed: Int): ResetResponse =
          val rng = SimpleRNG(Int.int2long(seed))
          val (obs, deInfos) = context.controller.reset(rng)
          val observations = obs.map(_.toObservationPair)
          val infos = deInfos.map { (ent, info) => ent.id.toString -> info }

          ResetResponse(observations = observations, infos = infos)

        private def manageStepRequest(actions: Map[String, ContinuousAction]): StepResponse =
          val agentActions = for
            (id, ca) <- actions
            agent <- context.controller.state.environment.entities.collect:
              case a: Agent if a.id.toString == id => a
            action <- MovementActionFactory.customMove[IO](ca.leftWheel, ca.rightWheel).toOption
          yield agent -> action
          val stepResponse: io.github.srs.controller.RLControllerModule.StepResponse =
            context.controller.step(agentActions)

          StepResponse(
            observations = stepResponse.observations.map(_.toObservationPair),
            rewards = stepResponse.rewards.map(_.to),
            terminateds = stepResponse.terminateds.map(_.to),
            truncateds = stepResponse.truncateds.map(_.to),
            infos = stepResponse.infos.map(_.to),
          )

        private def manageRenderRequest(width: Int, height: Int): RenderResponse =
          val imageBytes = context.controller.render(width, height)
          RenderResponse(
            image = ByteString.copyFrom(imageBytes),
            format = "png",
            width = width,
            height = height,
            channels = 3,
          )

        extension [A](self: (DynamicEntity, A))

          def to: (String, A) =
            val (de, a) = self
            de.id.toString -> a

          def to[B](f: A => B): (String, B) =
            val (de, a) = self
            de.id.toString -> f(a)

        extension (self: (DynamicEntity, SensorReadings))

          def toObservationPair: (String, Observation) =
            self.to(readings =>
              Observation(
                proximityValues = readings.proximityReadings.map(_.value),
                lightValues = readings.lightReadings.map(_.value),
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
