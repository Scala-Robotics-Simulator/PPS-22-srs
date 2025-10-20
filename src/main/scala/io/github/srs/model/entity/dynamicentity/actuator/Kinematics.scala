package io.github.srs.model.entity.dynamicentity.actuator

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.{Orientation, Point2D}

trait Kinematics[E <: DynamicEntity]:
  def position(e: E): Point2D
  def orientation(e: E): Orientation
  def radius(e: E): Double
  def withPose(e: E, pos: Point2D, orientation: Orientation): E

given Kinematics[Agent] with
  def position(e: Agent): Point2D = e.position
  def orientation(e: Agent): Orientation = e.orientation
  def radius(e: Agent): Double = e.shape.radius

  def withPose(e: Agent, pos: Point2D, orientation: Orientation): Agent =
    e.copy(position = pos, orientation = orientation)

given Kinematics[Robot] with
  def position(e: Robot): Point2D = e.position
  def orientation(e: Robot): Orientation = e.orientation
  def radius(e: Robot): Double = e.shape.radius

  def withPose(e: Robot, pos: Point2D, orientation: Orientation): Robot =
    (e at pos withOrientation orientation)
