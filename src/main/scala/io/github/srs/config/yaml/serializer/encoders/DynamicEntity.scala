package io.github.srs.config.yaml.serializer.encoders

import scala.language.postfixOps

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.StdProximitySensors as RobotStdProximitySensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.StdLightSensors as RobotStdLightSensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.StdProximitySensors as AgentStdProximitySensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Agent.StdLightSensors as AgentStdLightSensors
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Agent as AgentFields
import com.typesafe.scalalogging.Logger
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.reward.NoReward
import io.github.srs.model.entity.dynamicentity.agent.termination.NeverTerminate
import io.github.srs.model.entity.dynamicentity.agent.truncation.NeverTruncate

/**
 * Encoders for DynamicEntity types.
 */
object DynamicEntity:

  private val logger = Logger(getClass.getName)

  /**
   * Encoder for DynamicEntity types.
   * @return
   *   An Encoder that serializes DynamicEntity instances to JSON.
   */
  given Encoder[Robot] = (robot: Robot) =>
    val dwt = robot.actuators.collectFirst { case dwt: DifferentialWheelMotor[Robot] =>
      dwt
    }
    val speeds = dwt.map(d => (d.left.speed, d.right.speed))
    speeds match
      case Some(value) if value._1 != value._2 =>
        logger.warn(
          s"WARNING: encoding DifferentialWheelMotor with speeds (${value._1}, ${value._2}) the serializer only isn't able to correctly serialize them and will only use the left speed",
        )
      case _ => ()

    val withProximitySensors = RobotStdProximitySensors.forall(robot.sensors.contains)
    val withLightSensors = RobotStdLightSensors.forall(robot.sensors.contains)

    if robot.sensors.diff(RobotStdProximitySensors ++ RobotStdLightSensors).sizeIs > 0 then
      logger.warn(
        "WARNING: encoding robot with custom sensors, those will be lost during the serialization",
      )

    Json
      .obj(
        EntityFields.Id -> robot.id.asJson,
        EntityFields.Position -> robot.position.asJson,
        RobotFields.Radius -> robot.shape.radius.asJson,
        EntityFields.Orientation -> robot.orientation.degrees.asJson,
        RobotFields.WithProximitySensors -> withProximitySensors.asJson,
        RobotFields.WithLightSensors -> withLightSensors.asJson,
        RobotFields.Behavior -> robot.behavior.toString().asJson,
      )
      .deepMerge(
        speeds.map(RobotFields.Speed -> _._1.asJson).toList.toMap.asJson,
      )

  given Encoder[Agent] = (agent: Agent) =>
    val dwt = agent.actuators.collectFirst { case dwt: DifferentialWheelMotor[Agent] =>
      dwt
    }
    val speeds = dwt.map(d => (d.left.speed, d.right.speed))
    speeds match
      case Some(value) if value._1 != value._2 =>
        logger.warn(
          s"WARNING: encoding DifferentialWheelMotor with speeds (${value._1}, ${value._2}) the serializer isn't able to correctly serialize them and will only use the left speed",
        )
      case _ => ()

    val withProximitySensors = AgentStdProximitySensors.forall(agent.sensors.contains)
    val withLightSensors = AgentStdLightSensors.forall(agent.sensors.contains)

    if agent.sensors.diff(AgentStdProximitySensors ++ AgentStdLightSensors).sizeIs > 0 then
      logger.warn(
        "WARNING: encoding agent with custom sensors, those will be lost during the serialization",
      )

    val rewardName = agent.reward match
      case _: NoReward => "NoReward"
      case _ => "NoReward"

    val terminationName = agent.termination match
      case _: NeverTerminate => "NeverTerminate"
      case _ => "NeverTerminate"

    val truncationName = agent.truncation match
      case _: NeverTruncate => "NeverTruncate"
      case _ => "NeverTruncate"

    Json
      .obj(
        EntityFields.Id -> agent.id.asJson,
        EntityFields.Position -> agent.position.asJson,
        AgentFields.Radius -> agent.shape.radius.asJson,
        EntityFields.Orientation -> agent.orientation.degrees.asJson,
        AgentFields.WithProximitySensors -> withProximitySensors.asJson,
        AgentFields.WithLightSensors -> withLightSensors.asJson,
        AgentFields.Reward -> rewardName.asJson,
        AgentFields.Termination -> terminationName.asJson,
        AgentFields.Truncation -> truncationName.asJson,
      )
      .deepMerge(
        speeds.map(AgentFields.Speed -> _._1.asJson).toList.toMap.asJson,
      )

end DynamicEntity

export DynamicEntity.given
