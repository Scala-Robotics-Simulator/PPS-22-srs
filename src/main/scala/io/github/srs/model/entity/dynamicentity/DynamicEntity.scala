package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.environment.Environment
import io.github.srs.model.entity.dynamicentity.action.ActionAlgebra
import cats.effect.IO
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.robot.Robot

/**
 * Represents a dynamic entity. A dynamic entity is an entity that can change its state over time.
 */
trait DynamicEntity extends Entity:
  /**
   * Returns the sequence of actuators that can act upon a dynamic entity.
   * @return
   *   the sequence of actuators.
   */
  def actuators: Seq[Actuator[? <: DynamicEntity]]

  /**
   * Returns the sequence of sensors that can sense the environment for a dynamic entity.
   * @return
   *   the sequence of sensors.
   */
  def sensors: Vector[Sensor[? <: DynamicEntity, ? <: Environment]]

object DynamicEntity:

  given ActionAlgebra[IO, DynamicEntity] with

    override def moveWheels(e: DynamicEntity, leftSpeed: Double, rightSpeed: Double): IO[DynamicEntity] =
      e match
        case robot: Robot => summon[ActionAlgebra[IO, Robot]].moveWheels(robot, leftSpeed, rightSpeed)
        case agent: Agent => summon[ActionAlgebra[IO, Agent]].moveWheels(agent, leftSpeed, rightSpeed)
