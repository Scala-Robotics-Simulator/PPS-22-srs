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
