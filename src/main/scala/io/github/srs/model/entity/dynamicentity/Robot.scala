package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*

/**
 * Represents a robot entity in the simulation.
 *
 * @param shape
 *   the geometric shape of the robot, defined as a circle.
 * @param actuators
 *   the sequence of actuators that control the robot.
 */
final case class Robot(
    override val position: Point2D = defaultPosition,
    override val shape: ShapeType.Circle = defaultShape,
    override val orientation: Orientation = defaultOrientation,
    override val actuators: Seq[Actuator[Robot]] = defaultActuators,
    override val sensors: Vector[Sensor[Robot, Environment]] = defaultSensors,
) extends DynamicEntity
