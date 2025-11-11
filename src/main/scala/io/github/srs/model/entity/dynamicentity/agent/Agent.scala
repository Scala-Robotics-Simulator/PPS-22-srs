package io.github.srs.model.entity.dynamicentity.agent

import java.util.UUID

import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.ActionAlgebra
import io.github.srs.model.entity.dynamicentity.agent.reward.Reward
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent as AgentDefaults
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.agent.termination.Termination
import io.github.srs.model.entity.dynamicentity.agent.truncation.Truncation

/**
 * An `[[Agent]]` is a controllable dynamic entity with **no internal policy**. Its actions originate from an *external
 * controller*.
 *
 * @param id
 *   The unique identifier for this agent. Defaults to a randomly generated UUID.
 * @param position
 *   The position of the agent in 2D space, represented as a [[Point2D]].
 * @param shape
 *   The shape of the agent, represented as a [[ShapeType.Circle]] with a default radius.
 * @param orientation
 *   The orientation of the agent within the 2D space.
 * @param actuators
 *   The sequence of actuators that enables the agent to interact with the environment.
 * @param sensors
 *   The set of sensors that allows the agent to perceive its surroundings in the environment.
 */
final case class Agent(
    override val id: UUID = UUID.randomUUID(),
    override val position: io.github.srs.model.entity.Point2D = AgentDefaults.DefaultPosition,
    override val shape: ShapeType.Circle = AgentDefaults.DefaultShape,
    override val orientation: Orientation = AgentDefaults.DefaultOrientation,
    override val actuators: Seq[Actuator[Agent]] = AgentDefaults.DefaultActuators,
    override val sensors: Vector[Sensor[Agent, Environment]] = AgentDefaults.DefaultSensors,
    reward: Reward = AgentDefaults.DefaultReward,
    termination: Termination = AgentDefaults.DefaultTermination,
    truncation: Truncation = AgentDefaults.DefaultTruncation,
    lastAction: Option[Action[IO]] = None,
    aliveSteps: Int = 0,
    didCollide: Boolean = false,
) extends DynamicEntity

object Agent:

  /**
   * Creates a new instance of an Agent with the specified or default parameters.
   *
   * @param id
   *   The unique identifier for the agent.
   * @param position
   *   The position of the agent in 2D space.
   * @param shape
   *   The shape of the agent, represented as a [[ShapeType.Circle]].
   * @param orientation
   *   The orientation of the agent, measured in degrees.
   * @param actuators
   *   A sequence of actuators enabling the agent to perform actions. Defaults to an empty sequence.
   * @param sensors
   *   A collection of sensors that allow the agent to sense its environment. Defaults to an empty vector.
   * @return
   *   An instance of the [[Agent]] class with the specified or default attributes.
   */
  def apply(
      id: UUID = UUID.randomUUID(): UUID,
      position: Point2D = AgentDefaults.DefaultPosition,
      shape: ShapeType.Circle = AgentDefaults.DefaultShape,
      orientation: Orientation = AgentDefaults.DefaultOrientation,
      actuators: Seq[Actuator[Agent]] = AgentDefaults.DefaultActuators,
      sensors: Vector[Sensor[Agent, Environment]] = AgentDefaults.DefaultSensors,
      reward: Reward = AgentDefaults.DefaultReward,
      termination: Termination = AgentDefaults.DefaultTermination,
      truncation: Truncation = AgentDefaults.DefaultTruncation,
      action: Option[Action[IO]] = None,
      aliveSteps: Int = 0,
      didCollide: Boolean = false,
  ): Agent =
    new Agent(
      id,
      position,
      shape,
      orientation,
      actuators,
      sensors,
      reward,
      termination,
      truncation,
      action,
      aliveSteps,
      didCollide,
    )

  /**
   * Extractor method for the [[Agent]] .
   *
   * @param agent
   *   The [[Agent]] instance to be extracted.
   * @return
   *   An [[Option]] containing a tuple with the `Agent`'s attributes.
   */
  def unapply(agent: Agent): Option[
    (
        UUID,
        Point2D,
        ShapeType.Circle,
        Orientation,
        Seq[Actuator[Agent]],
        Vector[Sensor[Agent, Environment]],
    ),
  ] =
    Some((agent.id, agent.position, agent.shape, agent.orientation, agent.actuators, agent.sensors))

  given ActionAlgebra[IO, Agent] with

    override def moveWheels(agent: Agent, leftSpeed: Double, rightSpeed: Double): IO[Agent] =
      IO.pure:
        val updatedActuators = agent.actuators.map:
          case dwm: io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor[Agent] =>
            io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor(
              left = dwm.left.copy(speed = leftSpeed),
              right = dwm.right.copy(speed = rightSpeed),
            )
          case other => other
        agent.copy(actuators = updatedActuators)

end Agent
