package io.github.srs.controller

object ControllerModule:

  trait Controller:
    def start(): Unit
    def plotData(): Unit

  trait Provider:
    val controller: Controller

  type Requirements = io.github.srs.view.ViewModule.Provider & io.github.srs.model.ModelModule.Provider

  trait Component:
    context: Requirements =>

    object Controller:
      def apply(): Controller = new ControllerImpl

      private class ControllerImpl extends Controller:

        def start(): Unit =
          context.view.init()
          plotData()

        def plotData(): Unit =
          context.view.plotData(context.model.getData)

  trait Interface extends Provider with Component:
    self: Requirements =>
end ControllerModule
