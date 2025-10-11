package io.github.srs.model.dsl

import scala.language.{ implicitConversions, postfixOps }

import io.github.srs.model.entity.Point2D
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.EqualityGivenInstances.given

/**
 * Builder for creating an [[io.github.srs.model.environment.Environment]] from a grid of cells.
 * @param cells
 *   the grid of cells representing the environment.
 */
final case class EnvironmentBuilder(cells: Vector[Vector[Cell]]):

  /**
   * Builds the [[io.github.srs.model.environment.Environment]] from the grid of cells.
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
   * Implicit conversion from [[EnvironmentBuilder]] to [[io.github.srs.model.environment.Environment]].
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
  end extension

end GridDSL
