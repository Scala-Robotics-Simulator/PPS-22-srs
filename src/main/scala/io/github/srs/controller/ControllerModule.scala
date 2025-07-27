package io.github.srs.controller

import io.github.srs.model.ModelModule

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
    def start(initialState: S): Unit

    /**
     * Runs the simulation loop, updating the state and rendering the view.
     * @param s
     *   the current state of the simulation.
     */
    def simulationLoop(s: S): Unit

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

        /**
         * @inheritdoc
         */
        override def start(initialState: S): Unit =
          context.view.init()
          simulationLoop(initialState)

        /**
         * @inheritdoc
         */
        @annotation.tailrec
        override final def simulationLoop(s: S): Unit =
          val state = for
            newState <- context.model.update(s)
            _ <- Some(context.view.render(newState))
          yield newState

          state match
            case Some(ns) => simulationLoop(ns)
            case None => ()
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
