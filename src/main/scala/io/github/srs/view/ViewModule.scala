package io.github.srs.view

import cats.effect.IO
import cats.effect.std.Queue
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule

/**
 * Module that defines the view logic for the Scala Robotics Simulator.
 */
object ViewModule:

  /**
   * View trait that defines the interface for the view.
   *
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait View[S <: ModelModule.State]:

    /**
     * Initializes the view with a queue for handling events.
     * @param queue
     *   the queue that will be used to handle events in the view.
     * @return
     *   an [[IO]] task that completes when the initialization is done.
     */
    def init(queue: Queue[IO, Event]): IO[Unit]

    /**
     * Renders the view based on the current state.
     * @param state
     *   the current state of the simulation, which must extend [[ModelModule.State]].
     * @return
     *   the rendering task, which is an [[IO]] that completes when the rendering is done.
     */
    def render(state: S): IO[Unit]

  /**
   * Provider trait that defines the interface for providing a view.
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait Provider[S <: ModelModule.State]:
    val view: View[S]

  /**
   * Defines the dependencies required by the view module. In particular, it requires a
   * [[io.github.srs.controller.ControllerModule.Provider]].
   */
  type Requirements[S <: ModelModule.State] = io.github.srs.controller.ControllerModule.Provider[S]

  /**
   * Component trait that defines the interface for creating a view.
   *
   * @tparam S
   *   the type of the simulation state, which must extend [[ModelModule.State]].
   */
  trait Component[S <: ModelModule.State]:
    context: Requirements[S] =>

    /**
     * Factory method to create a new instance of the view.
     */
    object View:
      /**
       * Creates a new instance of the view.
       * @return
       *   the newly created view instance.
       */
      def apply(): View[S] = makeView()

    /**
     * Creates a new instance of the view.
     * @return
     *   the newly created view instance.
     */
    protected def makeView(): View[S]

  end Component

  /**
   * Interface trait that combines the provider and component traits for the view module.
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait Interface[S <: ModelModule.State] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end ViewModule
