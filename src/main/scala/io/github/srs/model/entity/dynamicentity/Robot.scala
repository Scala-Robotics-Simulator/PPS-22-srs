package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*

trait Robot extends DynamicEntity:
  override def shape: ShapeType.Circle
  override def actuators: Seq[Actuator[Robot]]

object Robot:

  private case class RobotImpl(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator[Robot]],
  ) extends Robot

  def apply(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator[Robot]],
  ): Robot = RobotImpl(position, shape, orientation, actuators)

  def unapply(robot: Robot): Option[(Point2D, ShapeType.Circle, Orientation, Seq[Actuator[Robot]])] =
    Some((robot.position, robot.shape, robot.orientation, robot.actuators))
