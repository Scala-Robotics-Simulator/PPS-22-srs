package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*

/**
 * Represents a robot entity in the simulation.
 */
trait Robot extends DynamicEntity:
  /**
   * The shape of the robot.
   * @return
   *   the [[ShapeType.Circle]] that defines the geometric shape of this entity.
   */
  override def shape: ShapeType.Circle

  /**
   * The sequence of actuators that control the robot.
   * @return
   *   the sequence of actuators.
   */
  override def actuators: Seq[Actuator[Robot]]

/**
 * Companion object for the [[Robot]] trait.
 */
object Robot:

  /**
   * Creates a new instance of a robot.
   *
   * @param position
   *   the position of the robot in the simulation.
   * @param shape
   *   the shape of the robot, defined as a circle.
   * @param orientation
   *   the orientation of the robot.
   * @param actuators
   *   the sequence of actuators that control the robot.
   * @return
   *   a new instance of [[Robot]].
   */
  private case class RobotImpl(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator[Robot]],
  ) extends Robot

  /**
   * Factory method to create a new robot instance.
   *
   * @param position
   *   the position of the robot.
   * @param shape
   *   the shape of the robot, defined as a circle.
   * @param orientation
   *   the orientation of the robot.
   * @param actuators
   *   the sequence of actuators that control the robot.
   * @return
   *   a new [[Robot]] instance.
   */
  def apply(
      position: Point2D,
      shape: ShapeType.Circle,
      orientation: Orientation,
      actuators: Seq[Actuator[Robot]],
  ): Robot = RobotImpl(position, shape, orientation, actuators)

  /**
   * Extractor method to deconstruct a [[Robot]] instance.
   *
   * @param robot
   *   the robot instance to extract from.
   * @return
   *   an option containing the position, shape, orientation, and actuators of the robot.
   */
  def unapply(robot: Robot): Option[(Point2D, ShapeType.Circle, Orientation, Seq[Actuator[Robot]])] =
    Some((robot.position, robot.shape, robot.orientation, robot.actuators))
end Robot
