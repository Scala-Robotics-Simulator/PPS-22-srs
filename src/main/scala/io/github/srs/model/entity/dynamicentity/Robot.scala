package io.github.srs.model.entity.dynamicentity

import java.util.UUID

import cats.effect.IO
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.action.{ Action, ActionAlg }
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Rule
import io.github.srs.model.entity.dynamicentity.sensor.{ Sensor, SensorReadings }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.withActuators

given CanEqual[Robot, Robot] = CanEqual.derived
given CanEqual[UUID, UUID] = CanEqual.derived

/**
 * Represents a robot entity in the simulation.
 *
 * @param shape
 *   the geometric shape of the robot, defined as a circle.
 * @param actuators
 *   the sequence of actuators that control the robot.
 */
final case class Robot(
    id: UUID = UUID.randomUUID(),
    override val position: Point2D = defaultPosition,
    override val shape: ShapeType.Circle = defaultShape,
    override val orientation: Orientation = defaultOrientation,
    override val actuators: Seq[Actuator[Robot]] = defaultActuators,
    override val sensors: Vector[Sensor[Robot, Environment]] = defaultSensors,
    override val behavior: Rule[IO, SensorReadings, Action[IO]] = defaultBehavior,
) extends DynamicEntity:

  override def equals(obj: Any): Boolean = obj match
    case that: Robot => this.id == that.id
    case _ => false

object Robot:

  given ActionAlg[IO, Robot] with

    override def moveWheels(robot: Robot, leftSpeed: Double, rightSpeed: Double): IO[Robot] =
      IO.pure:
        val updatedActuators = robot.actuators.map:
          case dwm: DifferentialWheelMotor =>
            DifferentialWheelMotor(
              left = dwm.left.copy(speed = leftSpeed),
              right = dwm.right.copy(speed = rightSpeed),
            )
          case other => other
        robot withActuators updatedActuators
