package io.github.srs.model.entity

import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent as AgentDefaults

/**
 * Represents an agent in the simulation environment.
 *
 * @param id
 *   The unique identifier for this agent. Defaults to a randomly generated UUID.
 * @param position
 *   The position of the agent in 2D space, represented as a [[Point2D]]. Defaults to `(0.0, 0.0)`.
 * @param shape
 *   The shape of the agent, represented as a [[ShapeType.Circle]] with a default radius of `0.25`.
 * @param orientation
 *   The orientation of the agent within the 2D space. Defaults to `0.0` degrees.
 * @param actuators
 *   The sequence of actuators that enables the agent to interact with the environment. Defaults to an empty sequence.
 * @param sensors
 *   The set of sensors that allows the agent to perceive its surroundings in the environment. Defaults to an empty
 *   vector.
 */
final case class Agent(
    override val id: java.util.UUID = java.util.UUID.randomUUID(),
    override val position: io.github.srs.model.entity.Point2D = AgentDefaults.DefaultPosition,
    override val shape: ShapeType.Circle = AgentDefaults.DefaultShape,
    override val orientation: Orientation = AgentDefaults.DefaultOrientation,
    override val actuators: Seq[Actuator[Agent]] = AgentDefaults.DefaultActuators,
    override val sensors: Vector[Sensor[Agent, Environment]] = AgentDefaults.DefaultSensors,
) extends DynamicEntity
