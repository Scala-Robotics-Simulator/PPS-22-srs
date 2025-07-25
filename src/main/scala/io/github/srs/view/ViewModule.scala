package io.github.srs.view

import io.github.srs.model.ModelModule

object ViewModule:

  trait View[S <: ModelModule.State]:
    def init(): Unit
    def render(state: S): Unit

  trait Provider[S <: ModelModule.State]:
    val view: View[S]

  type Requirements[S <: ModelModule.State] = io.github.srs.controller.ControllerModule.Provider[S]

  trait Component[S <: ModelModule.State]:
    context: Requirements[S] =>

    object View:
      def apply(): View[S] = new ViewImpl

      private class ViewImpl extends View[S]:
        private val gui = new SimpleView[S]

        override def init(): Unit = gui.init()

        override def render(state: S): Unit = gui.render(state)

  trait Interface[S <: ModelModule.State] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end ViewModule
