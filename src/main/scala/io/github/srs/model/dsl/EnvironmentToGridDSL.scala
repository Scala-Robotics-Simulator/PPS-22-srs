package io.github.srs.model.dsl

import scala.language.{implicitConversions, postfixOps}

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity.{Light, Obstacle}
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given

/**
 * Utility object for converting an [[io.github.srs.model.environment.Environment]] to a grid-based DSL representation
 * and pretty-printing it.
 */
object EnvironmentToGridDSL:

  /**
   * Converts an [[io.github.srs.model.environment.Environment]] to an [[EnvironmentBuilder]].
   * @param env
   *   the environment to convert.
   * @return
   *   the corresponding environment builder.
   */
  private def fromEnvironment(env: Environment): EnvironmentBuilder =

    def isVertical(angle: Double): Boolean =
      Math.round(angle) % 180 == 90

    def cellAt(x: Int, y: Int): Cell =
      def obstacleCovers(o: Obstacle): Boolean =
        val (wRaw, hRaw) =
          if isVertical(o.orientation.degrees) then (o.height, o.width)
          else (o.width, o.height)
        val w = Math.max(1, Math.ceil(wRaw).toInt)
        val h = Math.max(1, Math.ceil(hRaw).toInt)
        val x0 = Math.floor(o.position.x - wRaw / 2).toInt
        val y0 = Math.floor(o.position.y - hRaw / 2).toInt
        x >= x0 && x < x0 + w && y >= y0 && y < y0 + h

      val cell = env.entities.collectFirst:
        case r: Robot if Math.floor(r.position.x).toInt == x && Math.floor(r.position.y).toInt == y =>
          Cell.Robot(r.behavior)
        case l: Light if Math.floor(l.position.x).toInt == x && Math.floor(l.position.y).toInt == y => Cell.Light
        case o: Obstacle if obstacleCovers(o) => Cell.Obstacle
      cell.getOrElse(Cell.Empty)

    val grid: Vector[Vector[Cell]] =
      Vector.tabulate(env.height, env.width)((y, x) => cellAt(x, y))

    EnvironmentBuilder(grid)

  end fromEnvironment

  /**
   * Pretty-prints the given environment as a grid of cells.
   * @param environment
   *   the environment to pretty-print.
   * @return
   *   a string representation of the environment in grid format.
   */
  def prettyPrint(environment: Environment): String =
    val envBuilder: EnvironmentBuilder = fromEnvironment(environment)

    envBuilder.cells.map { row =>
        row.map {
          case Cell.Robot(policy) => Cell.symbolFor(policy)
          case Cell.Light => "**"
          case Cell.Obstacle => "X "
          case Cell.Empty => "--"
        }.mkString(" | ")
      }
      .mkString(" ||\n")
end EnvironmentToGridDSL