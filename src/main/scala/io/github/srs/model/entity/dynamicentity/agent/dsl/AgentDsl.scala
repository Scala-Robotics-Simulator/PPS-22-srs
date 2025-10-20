package io.github.srs.model.entity.dynamicentity.agent.dsl

import cats.syntax.all.*
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.dsl.ActuatorDsl.validateActuator
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.{differentialWheelMotor, ws}
import io.github.srs.model.entity.dynamicentity.actuator.{Actuator, DifferentialWheelMotor}
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.reward.RewardModel
import io.github.srs.model.entity.dynamicentity.sensor.{ProximitySensor, Sensor}
import io.github.srs.model.entity.{Orientation, Point2D, ShapeType}
import io.github.srs.model.environment.Environment
import io.github.srs.model.validation.Validation.*
import io.github.srs.model.validation.{DomainError, Validation}
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.*
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Agent.Self

import java.util.UUID

/**
 * The DSL for creating and configuring an Agent entity.
 * @example
 *   {{{
 *   import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*
 *
 *   val myAgent = agent at Point2D(1.0, 2.0) withShape ShapeType.Circle(0.5) withOrientation Orientation(90.0) containing WheelMotor() withSensors SensorSuite.empty
 *   myAgent.validate match
 *     case Right(validAgent) => println(s"Valid agent: $validAgent")
 *     case Left(error) => println(s"Validation error: $error.errorMessage")
 *   }}}
 */
object AgentDsl:

  /**
   * Validates an Agent entity to ensure it meets the domain constraints.
   * @param a
   *   the Agent entity to validate.
   * @return
   *   [[Right]] if the agent is valid, or [[Left]] with a validation
   */
  def validateAgent(a: Agent): Validation[Agent] = a.validate

  /**
   * Validates a Sensor to ensure it meets the domain constraints.
   *
   * @param sensor
   *   the Sensor to validate.
   * @return
   *   [[Right]] if the sensor is valid, or [[Left]] with a validation error.
   */
  def validateSensor(sensor: Sensor[Agent, Environment]): Validation[Sensor[Agent, Environment]] =
    sensor match
      case p: ProximitySensor[DynamicEntity, Environment] @unchecked =>
        io.github.srs.model.entity.dynamicentity.sensor.dsl.ProximitySensorDsl.validateProximitySensor(p)
      case s => Right[DomainError, Sensor[Agent, Environment]](s)

  /** Creates a new Agent with default properties. */
  def agent: Agent = Agent()

  /** Extension methods for Agent to allow DSL-like configuration. */
  extension (agent: Agent)

    infix def withId(id: UUID): Agent =
      agent.copy(id = id)

    /**
     * Sets the position of the agent.
     * @param position
     *   the new position of the agent.
     * @return
     *   a new [[Agent]] instance with the updated position.
     */
    infix def at(position: Point2D): Agent =
      agent.copy(position = position)

    /**
     * Sets the shape of the agent.
     * @param shape
     *   the new shape of the agent.
     * @return
     *   a new [[Agent]] instance with the updated shape.
     */
    infix def withShape(shape: ShapeType.Circle): Agent =
      agent.copy(shape = shape)

    /**
     * Sets the orientation of the agent.
     * @param orientation
     *   the new orientation of the agent.
     * @return
     *   a new [[Agent]] instance with the updated orientation.
     */
    infix def withOrientation(orientation: Orientation): Agent =
      agent.copy(orientation = orientation)

    /**
     * Sets the actuators of the agent.
     * @param actuators
     *   the new sequence of actuators for the agent.
     * @return
     *   a new [[Agent]] instance with the updated actuators.
     */
    infix def withActuators(actuators: Seq[Actuator[Agent]]): Agent =
      agent.copy(actuators = actuators)

    /**
     * Sets the sensors of the agent.
     * @param sensors
     *   the new sequence of sensors for the agent.
     * @return
     *   a new [[Agent]] instance with the updated sensors.
     */
    infix def withSensors(sensors: Seq[Sensor[Agent, Environment]]): Agent =
      agent.copy(sensors = agent.sensors ++ sensors.toVector)

    /**
     * Adds an actuator to the agent.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Agent]] instance with the actuator added.
     */
    infix def withActuator(actuator: Actuator[Agent]): Agent =
      agent.copy(actuators = agent.actuators :+ actuator)

    /**
     * Adds an actuator to the agent.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Agent]] instance with the actuator added.
     */
    infix def containing(actuator: Actuator[Agent]): Agent =
      withActuator(actuator)

    /**
     * Another way to add an actuator to the agent.
     * @param actuator
     *   the actuator to add.
     * @return
     *   a new [[Agent]] instance with the actuator added.
     */
    infix def and(actuator: Actuator[Agent]): Agent =
      containing(actuator)

    /**
     * Adds a sensor to the agent.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Agent]] instance with the sensor added.
     */
    infix def withSensor(sensor: Sensor[Agent, Environment]): Agent =
      agent.copy(sensors = agent.sensors :+ sensor)

    /**
     * Adds a sensor to the agent.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Agent]] instance with the sensor added.
     */
    infix def containing(sensor: Sensor[Agent, Environment]): Agent =
      withSensor(sensor)

    /**
     * Adds a sensor to the agent.
     * @param sensor
     *   the sensor to add.
     * @return
     *   a new [[Agent]] instance with the sensor added.
     */
    infix def and(sensor: Sensor[Agent, Environment]): Agent =
      containing(sensor)

    infix def withSpeed(speed: Double): Agent =
      val dfwOpt: Option[DifferentialWheelMotor[Agent]] =
        agent.actuators.collectFirst { case m: DifferentialWheelMotor[Agent] => m }

      val dfw: DifferentialWheelMotor[Agent] =
        dfwOpt.getOrElse(differentialWheelMotor[Agent])

      val updatedDfw: DifferentialWheelMotor[Agent] = dfw.ws(speed)
      val updatedActuators: Seq[Actuator[Agent]] =
        agent.actuators.filterNot(_ eq dfw) :+ updatedDfw

      agent.withActuators(updatedActuators)

    def withProximitySensors: Agent =
      agent.withSensors(StdProximitySensors)

    def withLightSensors: Agent =
      agent.withSensors(StdLightSensors)

    infix def withReward(reward: RewardModel[Agent]): Agent =
      agent.copy(reward = reward)

    /**
     * Validates the agent entity to ensure it meets the domain constraints.
     * @return
     *   [[Right]] if the agent is valid, or [[Left]] with a validation error message if it is not.
     */
    def validate: Validation[Agent] =
      val dwmCount = agent.actuators.collect { case _: DifferentialWheelMotor[?] => () }.size
      import Point2D.*
      for
        x <- notNaN(s"$Self x", agent.position.x)
        _ <- notInfinite(s"$Self x", x)
        y <- notNaN(s"$Self y", agent.position.y)
        _ <- notInfinite(s"$Self y", y)
        _ <- bounded(s"$Self radius", agent.shape.radius, MinRadius, MaxRadius, includeMax = true)
        _ <- notNaN(s"$Self degrees", agent.orientation.degrees)
        _ <- validateCount(s"$Self actuators", dwmCount, 0, 1)
        _ <- agent.actuators.traverse_(validateActuator)
        _ <- agent.sensors.traverse_(validateSensor)
      yield agent
  end extension

end AgentDsl
