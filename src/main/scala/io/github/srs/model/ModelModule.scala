package io.github.srs.model

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
     * Updates the state of the simulation.
     *
     * @param s
     *   the current state of the simulation.
     * @return
     *   an optional new state, or None if the simulation should end.
     */
    def update(s: S): Option[S]

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
       * Creates a model from the provided update function.
       *
       * @param updateFunc
       *   a function that defines how the state is updated.
       * @return
       *   a [[Model]] instance using the given update logic.
       */
      def apply(updateFunc: S => Option[S]): Model[S] = new ModelImpl(updateFunc)

      /**
       * Private model implementation that delegates state updates to the provided function.
       */
      private class ModelImpl(updateFunc: S => Option[S]) extends Model[S]:
        /**
         * @inheritdoc
         */
        override def update(s: S): Option[S] = updateFunc(s)
  end Component

  /**
   * Interface trait that combines the provider and component traits.
   * @tparam S
   *   the type of the state, which must extend [[State]].
   */
  trait Interface[S <: State] extends Provider[S] with Component[S]
end ModelModule
