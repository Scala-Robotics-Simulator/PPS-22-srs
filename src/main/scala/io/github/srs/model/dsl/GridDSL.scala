package io.github.srs.model.dsl

import scala.language.implicitConversions
import scala.language.postfixOps

import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.{ at, robot }
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.{ at, obstacle }
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_Cell_Cell

enum Cell:
  case Empty
  case Obstacle
  case Robot

  def toEntity(pos: Point2D): Set[Entity] = this match
    case Cell.Empty => Set.empty
    case Cell.Obstacle => Set(obstacle at pos)
    case Cell.Robot => Set(robot at pos)

object Cell:
  infix def -- : Cell = Cell.Empty
  infix def X: Cell = Cell.Obstacle
  infix def R: Cell = Cell.Robot

final case class EnvironmentBuilder(cells: Vector[Vector[Cell]]):

  def build(): Environment =
    val entities =
      for
        (row, y) <- cells.zipWithIndex
        (cell, x) <- row.zipWithIndex
        entity <- cell.toEntity(Point2D(x, y))
      yield entity

    Environment(
      width = cells.map(_.size).foldLeft(0)(math.max),
      height = cells.size,
      entities = entities.toSet,
    )

object GridDSL:

  given Conversion[EnvironmentBuilder, Environment] with
    def apply(builder: EnvironmentBuilder): Environment = builder.build()

  extension (first: Cell)

    infix def |(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(Vector(Vector(first, next)))

  extension (env: EnvironmentBuilder)

    infix def |(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(env.cells.dropRight(1) :+ (env.cells.lastOption.getOrElse(Vector.empty: Vector[Cell]) :+ next))

    infix def ||(next: Cell): EnvironmentBuilder =
      EnvironmentBuilder(env.cells :+ Vector(next))
