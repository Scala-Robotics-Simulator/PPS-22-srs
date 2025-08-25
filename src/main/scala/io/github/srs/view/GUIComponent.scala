package io.github.srs.view

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule
import io.github.srs.view.ViewModule.{ Component, Requirements, View }

/**
 * GUI component trait that defines the interface for creating a GUI view.
 *
 * @tparam S
 *   the type of the simulation state, which must extend [[ModelModule.State]].
 */
trait GUIComponent[S <: ModelModule.State] extends Component[S]:
  context: Requirements[S] =>

  /**
   * @inheritdoc
   */
  override protected def makeView(): View[S] = new GUIViewImpl

  /**
   * Implementation of the GUI view using a simulation view.
   */
  private class GUIViewImpl extends View[S]:
    private val gui = SimulationView[S]()

    /**
     * @inheritdoc
     */
    override def init(queue: Queue[IO, Event]): IO[Unit] = gui.init(queue)

    /**
     * @inheritdoc
     */
    override def render(state: S): IO[Unit] = gui.render(state)
end GUIComponent
