package io.github.srs.controller

import scala.concurrent.duration.{DurationInt, FiniteDuration, MILLISECONDS}
import scala.language.postfixOps
import cats.effect.std.Queue
import cats.effect.{Clock, IO}
import cats.syntax.all.*
import io.github.srs.controller.message.RobotProposal
import io.github.srs.controller.protocol.Event
import io.github.srs.model.*
import io.github.srs.model.SimulationConfig.SimulationStatus
import io.github.srs.model.UpdateLogic.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.logic.*
import io.github.srs.utils.SimulationDefaults.debugMode
import io.github.srs.utils.random.RNG
import io.github.srs.utils.random.RandomDSL.{generate, shuffle}

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

        override def start(initialState: S): IO[Unit] =
          for
            queueSim <- Queue.unbounded[IO, Event]
            _ <- context.view.init(queueSim)
            _ <- runBehavior(queueSim, initialState)
            _ <- simulationLoop(initialState, queueSim)
          yield ()

        override def simulationLoop(s: S, queue: Queue[IO, Event]): IO[Unit] =
          def loop(state: S): IO[Unit] =
            for
              startTime <- Clock[IO].realTime.map(_.toMillis)
              _ <- runBehavior(queue, state).whenA(state.simulationStatus == SimulationStatus.RUNNING)

              events <- queue.tryTakeN(Some(50))
              shuffledEvents <- shuffleEvents(queue, state, events)
              newState <- handleEvents(state, shuffledEvents)

              _ <- context.view.render(newState)

              nextState <- nextStep(newState, startTime)

              stop = newState.simulationStatus == SimulationStatus.STOPPED ||
                newState.simulationTime.exists(max => newState.elapsedTime >= max)

              endTime <- Clock[IO].realTime.map(_.toMillis)
              _ <- if debugMode then IO.println(s"Simulation loop took ${endTime - startTime} ms") else IO.unit

              _ <- if stop then IO.unit else loop(nextState)
            yield ()

          loop(s)

        end simulationLoop

        private def nextStep(state: S, startTime: Long): IO[S] =
          state.simulationStatus match
            case SimulationStatus.RUNNING =>
              tickEvents(startTime, state.simulationSpeed.tickSpeed, state)

            case SimulationStatus.PAUSED =>
              IO.sleep(50.millis).as(state)

            case SimulationStatus.STOPPED =>
              IO.pure(state)

        private def runBehavior(queue: Queue[IO, Event], state: S): IO[Unit] =
          for
            proposals <- state.environment.entities.collect { case robot: Robot =>
              for
                sensorReadings <- robot.senseAll[IO](state.environment)
                maybeAction <- robot.behavior.run(sensorReadings)
              yield maybeAction.map(a => RobotProposal(robot, a))
            }.toList.sequence.map(_.flatten)
            _ <- queue.offer(Event.RobotActionProposals(queue, proposals))
          yield ()

        private def tickEvents(start: Long, tickSpeed: FiniteDuration, state: S): IO[S] =
          for
            now <- Clock[IO].realTime.map(_.toMillis)
            timeToNextTick = tickSpeed.toMillis - (now - start)
            adjustedTickSpeed = if timeToNextTick > 0 then timeToNextTick else 0L
            sleepTime = FiniteDuration(adjustedTickSpeed, MILLISECONDS)
            _ <- IO.sleep(sleepTime)
            tick <- handleEvent(state, Event.Tick(tickSpeed))
          yield tick

        private def shuffleEvents(queue: Queue[IO, Event], state: S, events: Seq[Event]): IO[Seq[Event]] =
          val (controllerEvents, otherEvents) = events.partition:
            case _: Event.RobotAction => false
            case _ => true
          val (shuffledEvents, nextRNG: RNG) = state.simulationRNG generate (otherEvents shuffle)
          queue.offer(Event.Random(nextRNG)).as(controllerEvents ++ shuffledEvents)

        private def handleEvents(state: S, shuffledEvents: Seq[Event]): IO[S] =
          for finalState <- shuffledEvents.foldLeft(IO.pure(state)) { (taskState, event) =>
              for
                currentState <- taskState
                newState <- handleEvent(currentState, event)
              yield newState
            }
          yield finalState

        private def handleEvent(state: S, event: Event): IO[S] =
          event match
            case Event.Increment if state.simulationStatus == SimulationStatus.RUNNING =>
              context.model.increment(state)
            case Event.Tick(deltaTime) if state.simulationStatus == SimulationStatus.RUNNING =>
              context.model.tick(state, deltaTime)
            case Event.TickSpeed(speed) => context.model.tickSpeed(state, speed)
            case Event.Random(rng) => context.model.random(state, rng)
            case Event.Pause => context.model.pause(state)
            case Event.Resume => context.model.resume(state)
            case Event.Stop => context.model.stop(state)
            case Event.RobotActionProposals(queue, proposals) =>
              context.model.handleRobotActionsProposals(state, queue, proposals)
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
