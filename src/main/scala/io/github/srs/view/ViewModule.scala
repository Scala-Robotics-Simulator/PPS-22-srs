package io.github.srs.view

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
     * Initializes the view.
     */
    def init(): Unit

    /**
     * Renders the view based on the current state.
     *
     * @param state
     *   the current state of the simulation.
     */
    def render(state: S): Unit

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

    object View:
      /**
       * Creates a view instance.
       *
       * @return
       *   a [[View]] instance.
       */
      def apply(): View[S] = new ViewImpl

      /**
       * Private view implementation that uses a simple GUI.
       */
      private class ViewImpl extends View[S]:
        private val gui = new SimpleView[S]

        /**
         * Initializes the GUI.
         */
        override def init(): Unit = gui.init()

        /**
         * Renders the GUI based on the current state.
         *
         * @param state
         *   the current state of the simulation.
         */
        override def render(state: S): Unit = gui.render(state)

    end View

  end Component

  /**
   * Interface trait that combines the provider and component traits for the view module.
   * @tparam S
   *   the type of the state, which must extend [[ModelModule.State]].
   */
  trait Interface[S <: ModelModule.State] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end ViewModule
