package io.github.srs.model.entity.dynamic_entity

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }

trait Robot extends DynamicEntity:
  def moveTo(newPosition: Point2D): Robot

object Robot:

  def apply(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Option[Seq[Actuator]],
  ): Robot = RobotImpl(position, shape, orientation, actuators)

  case class RobotImpl(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Option[Seq[Actuator]],
  ) extends Robot:

    override def moveTo(newPosition: Point2D): Robot =
      this.copy(position = newPosition)
