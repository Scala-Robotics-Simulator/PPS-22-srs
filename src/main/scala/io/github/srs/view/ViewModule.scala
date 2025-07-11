package io.github.srs.view

object ViewModule:

  trait View:
    def init(): Unit
    def plotData(data: Int): Unit

  trait Provider:
    val view: View

  type Requirements = io.github.srs.controller.ControllerModule.Provider

  trait Component:
    context: Requirements =>

    object View:
      def apply(): View = new ViewImpl

      private class ViewImpl extends View:
        private val gui = new SimpleView

        def init(): Unit = gui.init()
        def plotData(data: Int): Unit = gui.plotData(data)

  trait Interface extends Provider with Component:
    self: Requirements =>
end ViewModule
