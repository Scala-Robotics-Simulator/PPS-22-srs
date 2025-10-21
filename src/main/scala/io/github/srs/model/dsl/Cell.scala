package io.github.srs.model.dsl

import scala.language.{ implicitConversions, postfixOps }

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.robot.behavior.Policy
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Entity, Point2D }
import io.github.srs.utils.EqualityGivenInstances.given
import io.github.srs.utils.SimulationDefaults.GridDSL.{ IncrementToCenterPos, ObstacleSize }
import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*

/**
 * Represents a cell in the grid-based DSL for defining environments.
 */
enum Cell:
  case Empty
  case Obstacle
  case Light
  case Robot(policy: Policy)
  case Agent

  /**
   * Converts the cell to a set of entities at the given position.
   * @param pos
   *   the position where the entity should be placed.
   * @return
   *   a set of entities corresponding to the cell type.
   */
  def toEntity(pos: Point2D): Set[Entity] = this match
    case Cell.Empty => Set.empty
    case Cell.Obstacle =>
      Set(
        obstacle at pos + IncrementToCenterPos withWidth ObstacleSize withHeight ObstacleSize,
      )
    case Cell.Light =>
      Set(
        light at pos + IncrementToCenterPos withRadius 0.2 withIntensity 1.0 withIlluminationRadius 6.0 withAttenuation 1.0,
      )
    case Cell.Robot(policy) =>
      Set(
        (robot at (pos + IncrementToCenterPos))
          .withSpeed(1.0)
          .withProximitySensors
          .withLightSensors
          .withBehavior(policy),
      )
    case Cell.Agent =>
      Set(
        (agent at (pos + IncrementToCenterPos))
          .withSpeed(1.0)
          .withProximitySensors
          .withLightSensors,
      )

end Cell

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
   * Represents a robot cell in the grid that always moves forward.
   *
   * @return
   *   the robot cell.
   */
  infix def A: Cell = Cell.Robot(Policy.AlwaysForward)

  /**
   * Represents a robot cell in the grid that walks randomly.
   * @return
   *   the robot cell.
   */
  infix def R: Cell = Cell.Robot(Policy.RandomWalk)

  /**
   * Represents a robot cell in the grid that avoids obstacles.
   * @return
   *   the robot cell.
   */
  infix def O: Cell = Cell.Robot(Policy.ObstacleAvoidance)

  /**
   * Represents a robot cell in the grid that moves towards light sources.
   * @return
   *   the robot cell.
   */
  infix def P: Cell = Cell.Robot(Policy.Phototaxis)

  /**
   * Represents a robot cell in the grid that uses prioritized behavior.
   * @return
   *   the robot cell.
   */
  infix def M: Cell = Cell.Robot(Policy.Prioritized)

  /**
   * Represents an agent cell in the grid.
   *
   * @return
   *   the agent cell.
   */
  infix def AG: Cell = Cell.Agent

  /**
   * Returns a symbol representing the given policy.
   * @param policy
   *   the policy to represent.
   * @return
   *   a string symbol for the policy.
   */
  def symbolFor(policy: Policy): String = policy match
    case Policy.AlwaysForward => "A "
    case Policy.RandomWalk => "R "
    case Policy.ObstacleAvoidance => "O "
    case Policy.Phototaxis => "P "
    case Policy.Prioritized => "M "

end Cell
