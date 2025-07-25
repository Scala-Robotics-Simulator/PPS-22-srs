package io.github.srs.controller

import io.github.srs.model.ModelModule

object ControllerModule:

  trait Controller[S <: ModelModule.State]:
    def start(initialState: S): Unit
    def simulationLoop(s: S): Unit

  trait Provider[S <: ModelModule.State]:
    val controller: Controller[S]

  type Requirements[S <: ModelModule.State] =
    io.github.srs.view.ViewModule.Provider[S] & io.github.srs.model.ModelModule.Provider[S]

  trait Component[S <: ModelModule.State]:
    context: Requirements[S] =>

    object Controller:
      def apply(): Controller[S] = new ControllerImpl

      private class ControllerImpl extends Controller[S]:

        override def start(initialState: S): Unit =
          context.view.init()
          simulationLoop(initialState)

        @annotation.tailrec
        override final def simulationLoop(s: S): Unit =
          val state = for
            newState <- context.model.update(s)
            _ <- Some(context.view.render(newState))
          yield newState

          state match
            case Some(ns) => simulationLoop(ns)
            case None => ()

  end Component

  trait Interface[S <: ModelModule.State] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
end ControllerModule
