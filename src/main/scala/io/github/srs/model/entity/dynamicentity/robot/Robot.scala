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
 * A [[Robot]] is an *autonomous* dynamic entity: its movement decisions are produced
 * sensors and invokes the policy to produce an [[Action]], which is then translated
 * into motion via the differential wheel actuator.
 *
 * @param id
 * the unique identifier of the robot. Defaults to a random UUID.
 * @param position
 * the current position of the robot in a two-dimensional space. Defaults to `DefaultPosition`.
 * @param shape
 * the geometric shape of the robot. Defaults to `DefaultShape`.
 * @param orientation
 * the current orientation of the robot in degrees. Defaults to `DefaultOrientation`.
 * @param actuators
 * the actuators attached to the robot, enabling it to interact with its environment. Defaults to `DefaultActuators`.
 * @param sensors
 * the sensors equipped on the robot, allowing it to sense the environment. Defaults to `DefaultSensors`.
 * @param behavior
 * the behavior policy defining the robot's actions based on contextual information. Defaults to `DefaultPolicy`.
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
