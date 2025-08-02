package io.github.srs.controller

import scala.compiletime.deferred
import scala.concurrent.duration.DurationInt

import cats.syntax.foldable.toFoldableOps
import io.github.srs.model.UpdateLogic.increment
import io.github.srs.model.{ IncrementLogic, ModelModule }
import monix.catnap.ConcurrentQueue
import monix.eval.Task

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
    def start(initialState: S): Task[Unit]

    /**
     * Runs the simulation loop, processing events from the queue and updating the state.
     *
     * @param s
     *   the current state of the simulation.
     * @param queue
     *   a concurrent queue that holds events to be processed.
     * @return
     *   a task that completes when the simulation loop ends.
     */
    def simulationLoop(s: S, queue: ConcurrentQueue[Task, Event]): Task[Unit]

  end Controller

  /**
   * Provider trait that defines the interface for providing a controller.
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
   * @tparam S
   *   the type of the simulation state, which must extend [[ModelModule.State]].
   */
  trait Component[S <: ModelModule.State]:
    context: Requirements[S] =>
    given inc: IncrementLogic[S] = deferred

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

        override def start(initialState: S): Task[Unit] =
          val list = List.fill(1_000)(Event.Increment)
          for
            queueSim <- ConcurrentQueue.unbounded[Task, Event]()
            _ <- context.view.init(queueSim)
            //            queueLog <- ConcurrentQueue.unbounded[Task, Event]()
            _ <- produceEvents(queueSim, list)
            _ <- simulationLoop(initialState, queueSim)
          //            _ <- Task.parMap2(
          //              simulationLoop(initialState, queueSim),
          //              consumeStream(queueLog)(event => Task(println(s"Received: $event")))
          //            )((_, _) => ())
          yield ()

        override def simulationLoop(s: S, queue: ConcurrentQueue[Task, Event]): Task[Unit] =
          def loop(state: S): Task[Unit] =
            for
              events <- queue.drain(0, 50)
              stop = events.contains(Event.Stop)
              newState <- handleEvents(events, state)
              _ <- context.view.render(newState)
              _ <- Task.sleep(100.millis)
              _ <- if stop then Task.unit else loop(newState)
            yield ()

          loop(s)

        //        private def consumeStream[A](queue: ConcurrentQueue[Task, A])(consume: A => Task[Unit]): Task[Unit] =
        //          Observable
        //            .repeatEvalF(queue.poll)
        //            .mapEval(consume)
        //            .completedL

        private def produceEvents[A](queue: ConcurrentQueue[Task, A], events: List[A]): Task[Unit] =
          events.traverse_(queue.offer)

        private def handleEvents(events: Seq[Event], state: S): Task[S] =
          for finalState <- events.foldLeft(Task.pure(state)) { (taskState, event) =>
              for
                currentState <- taskState
                newState <- handleEvent(event, currentState)
              yield newState
            }
          yield finalState

        private def handleEvent(event: Event, state: S): Task[S] =
          event match
            case Event.Increment => context.model.increment(state)
            case Event.Stop => Task.pure(state)

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
