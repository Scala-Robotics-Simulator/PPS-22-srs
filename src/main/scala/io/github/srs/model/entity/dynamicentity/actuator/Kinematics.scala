package io.github.srs.model.entity.dynamicentity.actuator

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.{ Orientation, Point2D }

/**
 * Provides kinematic operations for a dynamic entity.
 *
 * The kinematics typecast aims to decouple the kinematic operations from the specific dynamic entity implementations
 * and reuse them across different entity types.
 *
 * @tparam E
 *   The type of the dynamic entity this kinematics instance operates on.
 */
trait Kinematics[E <: DynamicEntity]:
  def position(e: E): Point2D
  def orientation(e: E): Orientation
  def radius(e: E): Double
  def withPose(e: E, pos: Point2D, orientation: Orientation): E

/**
 * Provides kinematic operations for the [[Agent]] type.
 */
given Kinematics[Agent] with
  def position(e: Agent): Point2D = e.position
  def orientation(e: Agent): Orientation = e.orientation
  def radius(e: Agent): Double = e.shape.radius

  def withPose(e: Agent, pos: Point2D, orientation: Orientation): Agent =
    e.copy(position = pos, orientation = orientation)

/**
 * Provides kinematic operations for the [[Robot]] type.
 */
given Kinematics[Robot] with
  def position(e: Robot): Point2D = e.position
  def orientation(e: Robot): Orientation = e.orientation
  def radius(e: Robot): Double = e.shape.radius

  def withPose(e: Robot, pos: Point2D, orientation: Orientation): Robot =
    e.copy(position = pos, orientation = orientation)

/**
 * Provides kinematic operations for the general [[DynamicEntity]] type by delegating to the specific implementations
 */
given Kinematics[DynamicEntity] with

  def position(e: DynamicEntity): Point2D = e match
    case r: Robot => summon[Kinematics[Robot]].position(r)
    case a: Agent => summon[Kinematics[Agent]].position(a)

  def orientation(e: DynamicEntity): Orientation = e match
    case r: Robot => summon[Kinematics[Robot]].orientation(r)
    case a: Agent => summon[Kinematics[Agent]].orientation(a)

  def radius(e: DynamicEntity): Double = e match
    case r: Robot => summon[Kinematics[Robot]].radius(r)
    case a: Agent => summon[Kinematics[Agent]].radius(a)

  def withPose(e: DynamicEntity, pos: Point2D, orientation: Orientation): DynamicEntity = e match
    case r: Robot => summon[Kinematics[Robot]].withPose(r, pos, orientation)
    case a: Agent => summon[Kinematics[Agent]].withPose(a, pos, orientation)
