package io.github.srs.view

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule
import io.github.srs.view.ViewModule.{ Component, Requirements, View }
import io.github.srs.utils.PrettyPrintExtensions.*
import io.github.srs.model.dsl.EnvironmentToGridDSL
import io.github.srs.utils.SimulationDefaults.DebugMode

/**
 * CLI component trait that defines the interface for creating a CLI view.
 *
 * @tparam S
 *   the type of the simulation state, which must extend [[io.github.srs.model.ModelModule.State]].
 */
trait CLIComponent[S <: ModelModule.State] extends Component[S]:
  context: Requirements[S] =>

  /**
   * @inheritdoc
   */
  override protected def makeView(): View[S] = new CLIViewImpl

  /**
   * Implementation of the CLI view that interacts via the command line.
   */
  private class CLIViewImpl extends View[S]:

    /**
     * @inheritdoc
     */
    override def init(queue: Queue[IO, Event]): IO[Unit] =
      for _ <- IO.println("Welcome to the Scala Robotics Simulator CLI Configuration")
      yield ()

    /**
     * @inheritdoc
     */
    override def render(state: S): IO[Unit] =
      for _ <-
          if DebugMode then IO.println(s"Current environment:\n${EnvironmentToGridDSL.prettyPrint(state.environment)}")
          else IO.unit
      yield ()

    /**
     * @inheritdoc
     */
    override def close(): IO[Unit] =
      for _ <- IO.println("Exiting the Scala Robotics Simulator CLI.")
      yield ()

    /**
     * @inheritdoc
     */
    override def timeElapsed(state: S): IO[Unit] =
      for
        _ <- IO.println(s"Simulation finished. Resulting state:\n${state.prettyPrint}")
        _ <- IO.println(s"Resulting environment:\n${EnvironmentToGridDSL.prettyPrint(state.environment)}")
      yield ()
  end CLIViewImpl
end CLIComponent
