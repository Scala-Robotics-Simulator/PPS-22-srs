package io.github.srs.model.dsl

import scala.language.implicitConversions
import scala.language.postfixOps

import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.StaticEntity.{ Boundary, Light, Obstacle }
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_Cell_Cell
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.environment.dsl.CreationDSL.*

/**
 * Represents a cell in the grid-based DSL for defining environments.
 */
enum Cell:
  case Empty
  case Obstacle
  case Light
  case Robot

  /**
   * Converts the cell to a set of entities at the given position.
   * @param pos
   *   the position where the entity should be placed.
   * @return
   *   a set of entities corresponding to the cell type.
   */
  def toEntity(pos: Point2D): Set[Entity] = this match
    case Cell.Empty => Set.empty
    case Cell.Obstacle => Set(obstacle at pos)
    case Cell.Light => Set(light at pos)
    case Cell.Robot => Set(robot at pos)

/**
 * Companion object for [[Cell]], providing convenient factory methods and operators.
 */
object Cell:

  /**
   * Represents an empty cell in the grid.
   * @return
   *   the empty cell.
   */
  infix def -- : Cell = Cell.Empty

  /**
   * Represents an obstacle cell in the grid.
   * @return
   *   the obstacle cell.
   */
  infix def X: Cell = Cell.Obstacle

  /**
   * Represents a light cell in the grid.
   * @return
   *   the light cell.
   */
  infix def ** : Cell = Cell.Light

  /**
   * Represents a robot cell in the grid.
   * @return
   *   the robot cell.
   */
  infix def R: Cell = Cell.Robot

end Cell

/**
 * Builder for creating an [[Environment]] from a grid of cells.
 * @param cells
 *   the grid of cells representing the environment.
 */
final case class EnvironmentBuilder(cells: Vector[Vector[Cell]]):

  /**
   * Builds the [[Environment]] from the grid of cells.
   * @return
   *   the constructed environment.
   */
  def build(): Environment =
    val entities =
      for
        (row, y) <- cells.zipWithIndex
        (cell, x) <- row.zipWithIndex
        entity <- cell.toEntity(Point2D(x, y))
      yield entity

    environment
      .withWidth(cells.map(_.size).foldLeft(0)(math.max))
      .withHeight(cells.size)
      .containing(entities.toSet)

/**
 * Domain-Specific Language (DSL) for creating grid-based environments.
 */
object GridDSL:

  /**
   * Implicit conversion from [[EnvironmentBuilder]] to [[Environment]].
   */
  given Conversion[EnvironmentBuilder, Environment] with
    def apply(builder: EnvironmentBuilder): Environment = builder.build()

  extension (first: Cell)

    /**
     * Starts a new row in the environment grid with the given cell.
     * @param next
     *   the next cell in the row.
     * @return
     *   an [[EnvironmentBuilder]] with the new row added.
     */
    infix def |(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(Vector(Vector(first, next)))

  extension (env: EnvironmentBuilder)

    /**
     * Adds a cell to the current row in the environment grid.
     * @param next
     *   the next cell to add to the current row.
     * @return
     *   an [[EnvironmentBuilder]] with the cell added to the current row.
     */
    infix def |(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(env.cells.dropRight(1) :+ (env.cells.lastOption.getOrElse(Vector.empty: Vector[Cell]) :+ next))

    /**
     * Starts a new row in the environment grid.
     * @param next
     *   the first cell of the new row.
     * @return
     *   an [[EnvironmentBuilder]] with the new row added.
     */
    infix def ||(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(env.cells :+ Vector(next))

end GridDSL

/**
 * Utility object for converting an [[Environment]] to a grid-based DSL representation and pretty-printing it.
 */
object EnvironmentToGridDSL:

  /**
   * Converts an [[Environment]] to an [[EnvironmentBuilder]].
   * @param env
   *   the environment to convert.
   * @return
   *   the corresponding environment builder.
   */
  private def fromEnvironment(env: Environment): EnvironmentBuilder =
    val grid: Vector[Vector[Cell]] =
      Vector.tabulate(env.height, env.width) { (y, x) =>
        val pos = Point2D(x, y)

        val maybeCell =
          env.entities
            .find(e =>
              e.position.x.toInt == pos.x &&
                e.position.y.toInt == pos.y,
            )
            .map:
              case _: Robot => Cell.Robot
              case _: Light => Cell.Light
              case _: Obstacle => Cell.Obstacle
              case _: Boundary => Cell.Empty

        maybeCell.getOrElse(Cell.Empty)
      }

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
        case Cell.Robot => "R "
        case Cell.Light => "**"
        case Cell.Obstacle => "X "
        case Cell.Empty => "--"
      }.mkString(" | ")
    }
      .mkString(" ||\n")
end EnvironmentToGridDSL
