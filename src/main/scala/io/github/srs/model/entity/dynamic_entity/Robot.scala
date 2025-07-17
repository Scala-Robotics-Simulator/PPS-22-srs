package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.*

trait Robot extends DynamicEntity:
  override def shape: ShapeType.Circle

object Robot:

  def apply(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator],
  ): Robot = RobotImpl(position, shape, orientation, actuators)

  def unapply(robot: Robot): Option[(Point2D, ShapeType.Circle, Orientation, Seq[Actuator])] =
    Some((robot.position, robot.shape, robot.orientation, robot.actuators))

  private case class RobotImpl(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator],
  ) extends Robot
