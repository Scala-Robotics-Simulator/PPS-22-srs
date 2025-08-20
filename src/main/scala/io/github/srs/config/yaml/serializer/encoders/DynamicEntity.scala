package io.github.srs.config.yaml.serializer.encoders

import scala.language.postfixOps

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.stdProximitySensors
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.stdLightSensors
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields

/**
 * Encoders for DynamicEntity types.
 */
object DynamicEntity:

  /**
   * Encoder for DynamicEntity types.
   * @return
   *   An Encoder that serializes DynamicEntity instances to JSON.
   */
  given Encoder[Robot] = (robot: Robot) =>
    val dwt = robot.actuators.collectFirst { case dwt: DifferentialWheelMotor =>
      dwt
    }
    val speeds = dwt.map(d => (d.left.speed, d.right.speed))
    speeds match
      case Some(value) if value._1 != value._2 =>
        println(
          s"WARNING: encoding DifferentialWheelMotor with speeds (${value._1}, ${value._2}) the serializer only isn't able to correctly serialize them and will only use the left speed",
        )
      case _ => ()

    val withProximitySensors = stdProximitySensors.forall(robot.sensors.contains)
    val withLightSensors = stdLightSensors.forall(robot.sensors.contains)

    if robot.sensors.diff(stdProximitySensors ++ stdLightSensors).sizeIs > 0 then
      println(
        "WARNING: encoding robot with custom sensors, those will be lost during the serialization",
      )

    // TODO: Serialize robot behavior
    Json
      .obj(
        EntityFields.id -> robot.id.asJson,
        EntityFields.position -> robot.position.asJson,
        RobotFields.radius -> robot.shape.radius.asJson,
        EntityFields.orientation -> robot.orientation.degrees.asJson,
        RobotFields.withProximitySensors -> withProximitySensors.asJson,
        RobotFields.withLightSensors -> withLightSensors.asJson,
      )
      .deepMerge(
        speeds.map(RobotFields.speed -> _._1.asJson).toList.toMap.asJson,
      )

end DynamicEntity

export DynamicEntity.given
