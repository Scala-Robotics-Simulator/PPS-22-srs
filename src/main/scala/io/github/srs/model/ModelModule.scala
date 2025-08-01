package io.github.srs.model

import monix.eval.Task

/**
 * Module that defines the model logic for the Scala Robotics Simulator.
 */
object ModelModule:

  /**
   * State trait that defines the base state for the simulation.
   */
  trait State

  /**
   * Trait representing the core model logic for updating the simulation state.
   *
   * @tparam S
   *   the type of the simulation state, which must extend [[State]].
   */
  trait Model[S <: State]:

    /**
     * Updates the state of the simulation using the provided function.
     * @param s
     *   the current state of the simulation.
     * @param f
     *   the function that takes the current state and returns a new state wrapped in a [[Task]].
     * @return
     *   the updated state wrapped in a [[Task]].
     */
    def update(s: S)(using f: S => Task[S]): Task[S]

  /**
   * Provider trait that defines the interface for providing a model.
   * @tparam S
   *   the type of the state, which must extend [[State]].
   */
  trait Provider[S <: State]:
    val model: Model[S]

  /**
   * Component trait that defines the interface for creating a model.
   *
   * @tparam S
   *   the type of the simulation state, which must extend [[State]].
   */
  trait Component[S <: State]:

    object Model:

      /**
       * Factory method to create a new instance of the model.
       *
       * @return
       *   a new instance of [[Model]].
       */
      def apply(): Model[S] = new ModelImpl

      /**
       * Private model implementation that delegates state updates to the provided function.
       */
      private class ModelImpl extends Model[S]:
        /**
         * @inheritdoc
         */
        override def update(s: S)(using updateLogic: S => Task[S]): Task[S] = updateLogic(s)
  end Component

  /**
   * Interface trait that combines the provider and component traits.
   * @tparam S
   *   the type of the state, which must extend [[State]].
   */
  trait Interface[S <: State] extends Provider[S] with Component[S]
end ModelModule
