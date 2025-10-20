package io.github.srs.model.entity.dynamicentity.robot

import java.util.UUID

import cats.effect.IO
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.action.ActionAlgebra
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.withActuators
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.robot.behavior.Policy
import io.github.srs.model.environment.Environment
import io.github.srs.utils.EqualityGivenInstances.given
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
    override val id: UUID = UUID.randomUUID(),
    override val position: Point2D = DefaultPosition,
    override val shape: ShapeType.Circle = DefaultShape,
    override val orientation: Orientation = DefaultOrientation,
    override val actuators: Seq[Actuator[Robot]] = DefaultActuators,
    override val sensors: Vector[Sensor[Robot, Environment]] = DefaultSensors,
    behavior: Policy = DefaultPolicy,
) extends DynamicEntity

object Robot:

  given ActionAlgebra[IO, Robot] with

    override def moveWheels(robot: Robot, leftSpeed: Double, rightSpeed: Double): IO[Robot] =
      IO.pure:
        val updatedActuators = robot.actuators.map:
          case dwm: DifferentialWheelMotor[Robot] =>
            DifferentialWheelMotor(
              left = dwm.left.copy(speed = leftSpeed),
              right = dwm.right.copy(speed = rightSpeed),
            )
          case other => other
        robot withActuators updatedActuators
