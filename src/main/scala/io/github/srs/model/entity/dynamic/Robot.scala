package io.github.srs.model.entity.dynamic

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }

trait Robot extends DynamicEntity:
  val actuators: Option[Seq[Actuator]]

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
  ) extends Robot
