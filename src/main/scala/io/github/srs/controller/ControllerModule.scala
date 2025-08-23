package io.github.srs.controller

import scala.concurrent.duration.{ DurationInt, FiniteDuration, MILLISECONDS }
import scala.language.postfixOps

import cats.effect.std.Queue
import cats.effect.{ Clock, IO }
import cats.syntax.all.*
import io.github.srs.controller.message.RobotProposal
import io.github.srs.controller.protocol.Event
import io.github.srs.model.*
import io.github.srs.model.SimulationConfig.SimulationStatus.{ PAUSED, RUNNING, STOPPED }
import io.github.srs.model.UpdateLogic.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.logic.*
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_Event_Event
import io.github.srs.utils.SimulationDefaults.debugMode
import io.github.srs.model.SimulationConfig.SimulationStatus

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

    object Controller:

      /**
       * Creates a controller instance.
       *
       * @return
       *   a [[Controller]] instance.
       */
      def apply()(using bundle: LogicsBundle[S]): Controller[S] = new ControllerImpl

      /**
       * Private controller implementation that delegates the simulation loop to the provided model and view.
       */
      private class ControllerImpl(using bundle: LogicsBundle[S]) extends Controller[S]:

        /**
         * Starts the controller with the initial state.
         * @param initialState
         *   the initial state of the simulation.
         * @return
         *   an [[IO]] task that completes when the controller is started.
         */
        override def start(initialState: S): IO[Unit] =
          for
            queueSim <- Queue.unbounded[IO, Event]
            _ <- context.view.init(queueSim)
            _ <- runBehavior(queueSim, initialState)
            _ <- simulationLoop(initialState, queueSim)
          yield ()

        /**
         * Runs the simulation loop, processing events from the queue and updating the state.
         * @param s
         *   the current state of the simulation.
         * @param queue
         *   a concurrent queue that holds events to be processed.
         * @return
         *   an [[IO]] task that completes when the simulation loop ends.
         */
        override def simulationLoop(s: S, queue: Queue[IO, Event]): IO[Unit] =
          def loop(state: S): IO[Unit] =
            for
              startTime <- Clock[IO].realTime.map(_.toMillis)
              _ <- updateField(queue, state)
              _ <- runBehavior(queue, state).whenA(state.simulationStatus == RUNNING)
              events <- queue.tryTakeN(Some(50))
              newState <- handleEvents(state, events)
              _ <- context.view.render(newState)
              nextState <- nextStep(newState, startTime)
              endTime <- Clock[IO].realTime.map(_.toMillis)
              _ <- if debugMode then IO.println(s"Simulation loop took ${endTime - startTime} ms") else IO.unit
              _ <- if stopCondition(nextState) then IO.unit else loop(nextState)
            yield ()

          loop(s)

        /**
         * Checks if the simulation should stop based on the current state.
         * @param state
         *   the current state of the simulation.
         * @return
         *   a boolean indicating whether the simulation should stop.
         */
        private def stopCondition(state: S): Boolean =
          state.simulationStatus == STOPPED ||
            elapsedTimeReached(state.simulationTime, state.elapsedTime)

        /**
         * Checks if the elapsed time has reached the maximum simulation time.
         * @param simulationTime
         *   the maximum simulation time, if defined.
         * @param elapsedTime
         *   the elapsed time since the simulation started.
         * @return
         *   a boolean indicating whether the elapsed time has reached the maximum simulation time.
         */
        private def elapsedTimeReached(simulationTime: Option[FiniteDuration], elapsedTime: FiniteDuration): Boolean =
          simulationTime.exists(max => elapsedTime >= max)

        /**
         * Processes the next step in the simulation based on the current state and start time.
         * @param state
         *   the current state of the simulation.
         * @param startTime
         *   the start time of the current simulation step in milliseconds.
         * @return
         *   the next state of the simulation wrapped in an [[IO]] task.
         */
        private def nextStep(state: S, startTime: Long): IO[S] =
          state.simulationStatus match
            case RUNNING =>
              tickEvents(startTime, state.simulationSpeed.tickSpeed, state)

            case PAUSED =>
              IO.sleep(50.millis).as(state)

            case STOPPED =>
              IO.pure(state)

        private def updateField(queue: Queue[IO, Event], state: S): IO[Unit] =
          for _ <-
              if state.simulationStatus == SimulationStatus.RUNNING then queue.offer(Event.UpdateLightField)
              else IO.unit
          yield ()

        /**
         * Runs the behavior of all robots in the environment and collects their action proposals.
         * @param queue
         *   the queue to which the proposals will be offered through the [[Event.RobotActionProposals]] event.
         * @param state
         *   the current state of the simulation.
         * @return
         *   an [[IO]] task that completes when the behavior has been run.
         */
        private def runBehavior(queue: Queue[IO, Event], state: S): IO[Unit] =
          for
            proposals <- state.environment.entities.collect { case robot: Robot => robot }.toList.parTraverse { robot =>
              for
                sensorReadings <- robot.senseAll[IO](state)
                action = robot.behavior.run(sensorReadings)
              yield RobotProposal(robot, action)
            }
            _ <- queue.offer(Event.RobotActionProposals(proposals))
          yield ()

        /**
         * Processes tick events, adjusting the tick speed based on the elapsed time since the last tick.
         * @param start
         *   the start time of the current tick in milliseconds.
         * @param tickSpeed
         *   the speed of the tick in [[FiniteDuration]].
         * @param state
         *   the current state of the simulation.
         * @return
         *   the next state of the simulation wrapped in an [[IO]] task.
         */
        private def tickEvents(start: Long, tickSpeed: FiniteDuration, state: S): IO[S] =
          for
            now <- Clock[IO].realTime.map(_.toMillis)
            timeToNextTick = tickSpeed.toMillis - (now - start)
            adjustedTickSpeed = if timeToNextTick > 0 then timeToNextTick else 0L
            sleepTime = FiniteDuration(adjustedTickSpeed, MILLISECONDS)
            _ <- IO.sleep(sleepTime)
            tick <- handleEvent(state, Event.Tick(tickSpeed))
          yield tick

        /**
         * Handles a sequence of events, processing them in the order they were received.
         * @param state
         *   the current state of the simulation.
         * @param events
         *   the sequence of events to be processed.
         * @return
         *   the final state of the simulation after processing all events, wrapped in an [[IO]] task.
         */
        private def handleEvents(state: S, events: Seq[Event]): IO[S] =
          for finalState <- events.foldLeft(IO.pure(state)) { (taskState, event) =>
              for
                currentState <- taskState
                newState <- handleEvent(currentState, event)
              yield newState
            }
          yield finalState

        /**
         * Handles a single event and updates the state accordingly.
         * @param state
         *   the current state of the simulation.
         * @param event
         *   the event to be processed.
         * @return
         *   the updated state of the simulation after processing the event, wrapped in an [[IO]] task.
         */
        private def handleEvent(state: S, event: Event): IO[S] =
          event match
            case Event.Tick(deltaTime) => context.model.tick(state, deltaTime)
            case Event.TickSpeed(speed) => context.model.tickSpeed(state, speed)
            case Event.Random(rng) => context.model.random(state, rng)
            case Event.Pause => context.model.pause(state)
            case Event.Resume => context.model.resume(state)
            case Event.Stop => context.model.stop(state)
            case Event.RobotActionProposals(proposals) => context.model.handleRobotActionsProposals(state, proposals)
            case Event.UpdateLightField => context.model.updateLightField(state)

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
