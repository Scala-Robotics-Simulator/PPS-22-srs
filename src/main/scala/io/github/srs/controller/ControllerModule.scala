package io.github.srs.controller

import scala.compiletime.deferred
import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all.*
import io.github.srs.model.*
import io.github.srs.model.SimulationConfig.SimulationStatus
import io.github.srs.model.UpdateLogic.*
import io.github.srs.model.logic.*
import io.github.srs.utils.SimulationDefaults.SimulationConfig.maxCount

/**
 * Module that defines the controller logic for the Scala Robotics Simulator.
 */
object ControllerModule:

  /**
   * Controller trait that defines the interface for the controller.
   *
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait Controller[S <: ModelModule.State]:
    /**
     * Starts the controller with the initial state.
     *
     * @param initialState
     *   the initial state of the simulation.
     */
    def start(initialState: S): IO[Unit]

    /**
     * Runs the simulation loop, processing events from the queue and updating the state.
     *
     * @param s
     *   the current state of the simulation.
     * @param queue
     *   a concurrent queue that holds events to be processed.
     * @return
     *   an [[IO]] task that completes when the simulation loop ends.
     */
    def simulationLoop(s: S, queue: Queue[IO, Event]): IO[Unit]

  end Controller

  /**
   * Provider trait that defines the interface for providing a controller.
   *
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait Provider[S <: ModelModule.State]:
    val controller: Controller[S]

  /**
   * Defines the dependencies required by the controller module. In particular, it requires a
   * [[io.github.srs.view.ViewModule.Provider]] and a [[io.github.srs.model.ModelModule.Provider]].
   */
  type Requirements[S <: ModelModule.State] =
    io.github.srs.view.ViewModule.Provider[S] & io.github.srs.model.ModelModule.Provider[S]

  /**
   * Component trait that defines the interface for creating a controller.
   *
   * @tparam S
   *   the type of the simulation state, which must extend [[ModelModule.State]].
   */
  trait Component[S <: ModelModule.State]:
    context: Requirements[S] =>
    given inc: IncrementLogic[S] = deferred

    given tick: TickLogic[S] = deferred

    given pause: PauseLogic[S] = deferred

    given resume: ResumeLogic[S] = deferred

    given stop: StopLogic[S] = deferred

    object Controller:
      /**
       * Creates a controller instance.
       *
       * @return
       *   a [[Controller]] instance.
       */
      def apply(): Controller[S] = new ControllerImpl

      /**
       * Private controller implementation that delegates the simulation loop to the provided model and view.
       */
      private class ControllerImpl extends Controller[S]:

        override def start(initialState: S): IO[Unit] =
          val randInt: Int = initialState.simulationRNG.nextIntBetween(0, maxCount)._1
          val list = List.fill(randInt)(Event.Increment)
          for
            queueSim <- Queue.unbounded[IO, Event]
            _ <- context.view.init(queueSim)
            _ <- produceEvents(queueSim, list)
            _ <- simulationLoop(initialState, queueSim)
          yield ()

        override def simulationLoop(s: S, queue: Queue[IO, Event]): IO[Unit] =
          def loop(state: S): IO[Unit] =
            for
              events <- queue.tryTakeN(Some(50))
              newState <- handleEvents(events, state)
              _ <- context.view.render(newState)
              nextState <-
                if newState.simulationStatus == SimulationStatus.RUNNING then
                  tickEvents(newState.simulationSpeed.tickSpeed, newState)
                else IO.pure(newState)
              stop = newState.simulationStatus == SimulationStatus.STOPPED ||
                newState.simulationTime.exists(max => newState.elapsedTime >= max)
              _ <- if stop then IO.unit else loop(nextState)
            yield ()

          loop(s)

        private def produceEvents[A](queue: Queue[IO, A], events: List[A]): IO[Unit] =
          events.traverse_(queue.offer)

        private def tickEvents(tickSpeed: FiniteDuration, state: S): IO[S] =
          for
            _ <- IO.sleep(tickSpeed)
            tick <- handleEvent(Event.Tick(tickSpeed), state)
          yield tick

        private def handleEvents(events: Seq[Event], state: S): IO[S] =
          for finalState <- events.foldLeft(IO.pure(state)) { (taskState, event) =>
              for
                currentState <- taskState
                newState <- handleEvent(event, currentState)
              yield newState
            }
          yield finalState

        private def handleEvent(event: Event, state: S): IO[S] =
          event match
            case Event.Increment if state.simulationStatus == SimulationStatus.RUNNING =>
              context.model.increment(state)
            case Event.Tick(deltaTime) if state.simulationStatus == SimulationStatus.RUNNING =>
              context.model.tick(state, deltaTime)
            case Event.Pause => context.model.pause(state)
            case Event.Resume => context.model.resume(state)
            case Event.Stop => context.model.stop(state)
            case Event.TickSpeed(speed) => context.model.tickSpeed(state, speed)
            case _ => IO.pure(state)

      end ControllerImpl

    end Controller

  end Component

  /**
   * Interface trait that combines the provider and component traits for the controller module.
   *
   * @tparam S
   *   the type of the simulation state, which must extend [[ModelModule.State]].
   */
  trait Interface[S <: ModelModule.State] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end ControllerModule
