package io.github.srs.view

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.dsl.EnvironmentToGridDSL.prettyPrint
import io.github.srs.view.ViewModule.{ Component, Requirements, View }

/**
 * CLI component trait that defines the interface for creating a CLI view.
 *
 * @tparam S
 *   the type of the simulation state, which must extend [[ModelModule.State]].
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
      IO.unit

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
      for _ <- IO.println(s"Simulation finished. Resulting environment:\n${prettyPrint(state.environment)}")
      yield ()
  end CLIViewImpl
end CLIComponent
