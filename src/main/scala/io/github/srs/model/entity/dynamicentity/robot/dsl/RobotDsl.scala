package io.github.srs.model.entity.dynamicentity.robot.dsl

import java.util.UUID

import cats.syntax.all.*
import io.github.srs.model.entity.dynamicentity.actuator.dsl.ActuatorDsl.validateActuator
import io.github.srs.model.entity.dynamicentity.sensor.dsl.SensorDsl.validateSensor
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.{ differentialWheelMotor, ws }
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.robot.behavior.Policy
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot.Self

/**
 * The DSL for creating and configuring a Robot entity.
 * @example
 *   {{{
 *   import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
 *
 *   val myRobot = robot at Point2D(1.0, 2.0) withShape ShapeType.Circle(0.5) withOrientation Orientation(90.0) containing WheelMotor() withSensors SensorSuite.empty
 *   myRobot.validate match
 *     case Right(validRobot) => println(s"Valid robot: $validRobot")
 *     case Left(error) => println(s"Validation error: $error.errorMessage")
 *   }}}
 */
object RobotDsl:

  /**
   * Validates a Robot entity to ensure it meets the domain constraints.
   * @param r
   *   the Robot entity to validate.
   * @return
   *   [[Right]] if the robot is valid, or [[Left]] with a validation
   */
  def validateRobot(r: Robot): Validation[Robot] = r.validate

  /** Creates a new Robot with default properties. */
  def robot: Robot = Robot()

  /** Extension methods for Robot to allow DSL-like configuration. */
  extension (robot: Robot)

    infix def withId(id: UUID): Robot =
      robot.copy(id = id)

    /**
     * Sets the position of the robot.
     * @param position
     *   the new position of the robot.
     * @return
     *   a new [[Robot]] instance with the updated position.
     */
    infix def at(position: Point2D): Robot =
      robot.copy(position = position)

    /**
     * Sets the shape of the robot.
     * @param shape
     *   the new shape of the robot.
     * @return
     *   a new [[Robot]] instance with the updated shape.
     */
    infix def withShape(shape: ShapeType.Circle): Robot =
      robot.copy(shape = shape)

    /**
     * Sets the orientation of the robot.
     * @param orientation
     *   the new orientation of the robot.
     * @return
     *   a new [[Robot]] instance with the updated orientation.
     */
    infix def withOrientation(orientation: Orientation): Robot =
      robot.copy(orientation = orientation)

    /**
     * Sets the actuators of the robot.
     * @param actuators
     *   the new sequence of actuators for the robot.
     * @return
     *   a new [[Robot]] instance with the updated actuators.
     */
    infix def withActuators(actuators: Seq[Actuator[Robot]]): Robot =
      robot.copy(actuators = actuators)

    /**
     * Sets the sensors of the robot.
     * @param sensors
     *   the new sequence of sensors for the robot.
     * @return
     *   a new [[Robot]] instance with the updated sensors.
     */
    infix def withSensors(sensors: Seq[Sensor[Robot, Environment]]): Robot =
      robot.copy(sensors = robot.sensors ++ sensors.toVector)

    /**
     * Adds an actuator to the robot.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Robot]] instance with the actuator added.
     */
    infix def withActuator(actuator: Actuator[Robot]): Robot =
      robot.copy(actuators = robot.actuators :+ actuator)

    /**
     * Adds an actuator to the robot.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Robot]] instance with the actuator added.
     */
    infix def containing(actuator: Actuator[Robot]): Robot =
      withActuator(actuator)

    /**
     * Another way to add an actuator to the robot.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Robot]] instance with the actuator added.
     */
    infix def and(actuator: Actuator[Robot]): Robot =
      containing(actuator)

    /**
     * Adds a sensor to the robot.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Robot]] instance with the sensor added.
     */
    infix def withSensor(sensor: Sensor[Robot, Environment]): Robot =
      robot.copy(sensors = robot.sensors :+ sensor)

    /**
     * Adds a sensor to the robot.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Robot]] instance with the sensor added.
     */
    infix def containing(sensor: Sensor[Robot, Environment]): Robot =
      withSensor(sensor)

    /**
     * Adds a sensor to the robot.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Robot]] instance with the sensor added.
     */
    infix def and(sensor: Sensor[Robot, Environment]): Robot =
      containing(sensor)

    infix def withSpeed(speed: Double): Robot =
      val dfwOpt: Option[DifferentialWheelMotor[Robot]] =
        robot.actuators.collectFirst { case m: DifferentialWheelMotor[Robot] => m }

      val dfw: DifferentialWheelMotor[Robot] =
        dfwOpt.getOrElse(differentialWheelMotor[Robot])

      val updatedDfw: DifferentialWheelMotor[Robot] = dfw.ws(speed)
      val updatedActuators: Seq[Actuator[Robot]] =
        robot.actuators.filterNot(_ eq dfw) :+ updatedDfw

      robot.withActuators(updatedActuators)

    def withProximitySensors: Robot =
      robot.withSensors(StdProximitySensors)

    def withLightSensors: Robot =
      robot.withSensors(StdLightSensors)

    infix def withBehavior(behavior: Policy): Robot =
      robot.copy(behavior = behavior)

    /**
     * Validates the robot entity to ensure it meets the domain constraints.
     * @return
     *   [[Right]] if the robot is valid, or [[Left]] with a validation error message if it is not.
     */
    def validate: Validation[Robot] =
      val dwmCount: Int = robot.actuators.count { case _: DifferentialWheelMotor[?] => true; case _ => false }
      import Point2D.*
      for
        x <- notNaN(s"$Self x", robot.position.x)
        _ <- notInfinite(s"$Self x", x)
        y <- notNaN(s"$Self y", robot.position.y)
        _ <- notInfinite(s"$Self y", y)
        _ <- bounded(s"$Self radius", robot.shape.radius, MinRadius, MaxRadius, includeMax = true)
        _ <- notNaN(s"$Self degrees", robot.orientation.degrees)
        _ <-
          if dwmCount <= 1 then Right(robot)
          else Left(io.github.srs.model.validation.DomainError.InvalidCount(s"$Self actuators", dwmCount, 0, 1))
        _ <- robot.actuators.traverse_(validateActuator)
        _ <- robot.sensors.traverse_(validateSensor)
      yield robot
  end extension
end RobotDsl
