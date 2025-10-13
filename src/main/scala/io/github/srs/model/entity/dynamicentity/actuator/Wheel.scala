package io.github.srs.model.entity.dynamicentity.actuator

import io.github.srs.model.entity.ShapeType
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor.Wheel.*

/**
 * Represents a wheel with linear speed and circular shape.
 *
 * @param speed
 *   the linear speed of the wheel (in meters per second).
 * @param shape
 *   the physical shape of the wheel, assumed to be a circle.
 */
final case class Wheel(speed: Double = DefaultSpeed, shape: ShapeType.Circle = DefaultShape)
