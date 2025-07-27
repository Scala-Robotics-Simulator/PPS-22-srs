package io.github.srs.view

/**
 * The [[ViewModule]] provides abstractions and implementations for the application's viewing component.
 * It is responsible for initializing and displaying formatted data through the GUI.
 */
object ViewModule:

  trait View:
    def init(): Unit
    def plotData(text: String): Unit          // ← renamed API

  trait Provider:
    val view: View

  type Requirements = io.github.srs.controller.ControllerModule.Provider

  trait Component:
    context: Requirements =>

    object View:
      def apply(): View = new ViewImpl

      private class ViewImpl extends View:
        private val gui = new SimpleView
        def init(): Unit                     = gui.init()
        def plotData(text: String): Unit     = gui.plotData(text)
  end Component

  trait Interface extends Provider with Component:
    self: Requirements =>
end ViewModule
