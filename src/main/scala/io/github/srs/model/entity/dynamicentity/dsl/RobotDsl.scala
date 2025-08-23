package io.github.srs.model.entity.dynamicentity.dsl

import java.util.UUID

import io.github.srs.model.ModelModule
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.{ differentialWheelMotor, ws }
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.{ notInfinite, notNaN, validateCountOfType }
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*

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
    infix def withSensors(sensors: Seq[Sensor[Robot, ModelModule.State]]): Robot =
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
    infix def withSensor(sensor: Sensor[Robot, ModelModule.State]): Robot =
      robot.copy(sensors = robot.sensors :+ sensor)

    /**
     * Adds a sensor to the robot.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Robot]] instance with the sensor added.
     */
    infix def containing(sensor: Sensor[Robot, ModelModule.State]): Robot =
      withSensor(sensor)

    /**
     * Adds a sensor to the robot.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Robot]] instance with the sensor added.
     */
    infix def and(sensor: Sensor[Robot, ModelModule.State]): Robot =
      containing(sensor)

    infix def withSpeed(speed: Double): Robot =
      val dfw =
        robot.actuators.collectFirst { case dfw: DifferentialWheelMotor => dfw }.getOrElse(differentialWheelMotor)
      val updatedDfw = dfw.ws(speed)
      val updatedActuators = robot.actuators.filterNot(_.equals(dfw)) :+ updatedDfw
      robot.withActuators(updatedActuators)

    def withProximitySensors: Robot =
      robot.withSensors(stdProximitySensors)

    def withLightSensors: Robot =
      robot.withSensors(stdLightSensors)

    infix def withBehavior(behavior: Policy): Robot =
      robot.copy(behavior = behavior)

    /**
     * Validates the robot entity to ensure it meets the domain constraints.
     * @return
     *   [[Right]] if the robot is valid, or [[Left]] with a validation error message if it is not.
     */
    def validate: Validation[Robot] =
      import Point2D.*
      for
        x <- notNaN("x", robot.position.x)
        _ <- notInfinite("x", x)
        y <- notNaN("y", robot.position.y)
        _ <- notInfinite("y", y)
        _ <- notNaN("degrees", robot.orientation.degrees)
        _ <- validateCountOfType[DifferentialWheelMotor]("actuators", robot.actuators, 0, 1)
      yield robot
  end extension
end RobotDsl
