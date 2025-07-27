package io.github.srs.controller

import io.github.srs.model.Cell
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.staticentity.StaticEntity.{Light, Obstacle}
import io.github.srs.model.environment.Environment
import io.github.srs.model.lighting.{LightState, ShadowFovDiffuser}
import io.github.srs.view.ViewModule

object ControllerModule:

  /** Public controller interface */
  trait Controller:
    def start(): Unit

  /** Will be mixed in by the launcher to provide a `controller` */
  trait Provider:
    val controller: Controller

  /** The dependencies this module needs: a View and a Model */
  type Requirements = ViewModule.Provider & io.github.srs.model.ModelModule.Provider

  /** The implementation */
  trait Component:
    context: Requirements =>

    object Controller:
      def apply(): Controller = new ControllerImpl

      private class ControllerImpl extends Controller:

        def start(): Unit =
          context.view.init()
          showLightMap()

        private def showLightMap(): Unit =
          import io.github.srs.model.environment.view
          import io.github.srs.model.entity.Point2D.toCell

          // 1) build & validate environment
          val env = Environment(
            width    = 10,
            height   = 10,
            entities = Set(
              Obstacle((2.0, 1.0), Orientation(0), 1, 1),
              Obstacle((2.0, 2.0), Orientation(0), 1, 1),
              Obstacle((6.0, 4.0), Orientation(0), 1, 1),
              Light   ((0.0, 0.0), Orientation(0), 8.0, intensity = 1.0, attenuation = 0.2),
              Light   ((9.0, 9.0), Orientation(0), 2.0, intensity = 1.0, attenuation = 0.2)
            )
          ).fold(err => sys.error(err.errorMessage), identity)

          val view      = env.view
          val diffuser  = ShadowFovDiffuser()
          val lightInit = LightState.empty(view.width, view.height)
          val lightMap  = diffuser.step(view)(lightInit)

          val obstacles = env.entities.collect {
            case Obstacle((x, y), _, w, h) =>
              f"(${x}%.2f, ${y}%.2f)  size=${w}×$h"
          }.toVector

          val lights = env.entities.collect {
            case Light((x, y), _, r, i, k) =>
              f"(${x}%.2f, ${y}%.2f)  r=$r%.1f  I=$i%.1f  k=$k%.2f"
          }.toVector

          val header =
            s"""|Grid      : ${env.width}×${env.height}
                |
                |Obstacles : ${obstacles.size}
                |${obstacles.map("  • " + _).mkString("\n")}
                |
                |Lights    : ${lights.size}
                |${lights.map("  • " + _).mkString("\n")}
                |
                |Legend
                |  #  obstacle
                |  L  light source
                |  .░▒▓█  darkest → brightest
                |
                |""".stripMargin

          val asciiRaw = lightMap.render(view)
          val numeric  = lightMap.render(view, ascii = false)

          val lightCells = view.lights.map(_.position.toCell).toSet
          val ascii = overlayLights(asciiRaw, lightCells)

          context.view.plotData(
            header +
              ascii   + "\n\n" +
              numeric
          )

        /** Replace the shade‐char at each light‐cell with 'L' */
        private def overlayLights(ascii: String, lights: Set[Cell]): String =
          ascii
            .linesIterator
            .zipWithIndex
            .map { case (line, y) =>
              line.zipWithIndex.map { case (ch, x) =>
                if lights.contains(Cell(x, y)) then 'L' else ch
              }.mkString
            }
            .mkString("\n")

      end ControllerImpl

  /** Glue the provider + component together */
  trait Interface extends Provider with Component:
    self: Requirements =>
end ControllerModule
