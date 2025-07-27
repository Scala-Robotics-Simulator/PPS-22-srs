package io.github.srs.model.environment

import io.github.srs.model.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.staticentity.StaticEntity.{Light, Obstacle}
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot

/**
 * Represents a view of the environment at a specific tick.
 *
 * This view contains all the necessary data to understand the state of the environment
 * at a given moment, including:
 * * - The dimensions of the environment (width and height)
 * * - The set of obstacles present in the environment
 * * - The static lights that illuminate the environment
 * * - The resistance grid, which indicates the resistance of each cell in the environment
 */
final case class EnvironmentView(
    width: Int,
    height: Int,
    obstacles: Set[Cell],
    robots: Vector[Robot],
    lights: Vector[StaticEntity.Light],
    resistance: Array[Array[Double]],
)

object EnvironmentView:

  /**
   * Static snapshot: robots **ARE NOT** considered opaque.
   * This means that robots do not block light.
   *
   *  @param env
   *  the environment to build the view from.
   *
   *  @return
   *  an [[EnvironmentView]] representing the static state of the environment.
   */
  def static(env: Environment): EnvironmentView =
    build(env, blockRobots = false)

  /**
   * Dynamic snapshot: robots **ARE** considered opaque.
   * This means that robots block light.
   *
   * @param env
   * the environment to build the view from.
   *
   * @return
   * an [[EnvironmentView]] representing the dynamic state of the environment.
   */
  def dynamic(env: Environment): EnvironmentView =
    build(env, blockRobots = true)

  private def build(
                     env: Environment,
                     blockRobots: Boolean
                   ): EnvironmentView =
    val (width, height) = (env.width, env.height)

    val obstacles: Set[Cell] =
      for
        Obstacle(pos, _, w0, h0) <- env.entities.collect { case obstacle: Obstacle => obstacle }
        dx <- 0 until w0.toInt
        dy <- 0 until h0.toInt
      yield Cell((pos.x + dx).round.toInt, (pos.y + dy).round.toInt)

    val robots: Vector[Robot] =
      env.entities.collect { case r: Robot => r }.toVector

    val lights: Vector[Light] =
      env.entities.collect { case l: Light => l }.toVector
    
    val robotCells: Set[Cell] = robots.map(_.position.toCell).toSet

    val resistance: Array[Array[Double]] =
      Array.tabulate(width, height) { (x, y) =>
        val cell = Cell(x, y)
        val solid =
          obstacles.contains(cell) ||
            (blockRobots && robotCells.contains(cell))
        if solid then 1.0 else 0.0
      }
    
    EnvironmentView(width, height, obstacles, robots, lights, resistance)
end EnvironmentView