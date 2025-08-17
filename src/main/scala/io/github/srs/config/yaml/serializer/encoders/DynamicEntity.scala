package io.github.srs.config.yaml.serializer.encoders

import scala.language.postfixOps

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.sensor.Sensor

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
    val baseFields = List(
      "id" -> robot.id.asJson,
      "position" -> robot.position.asJson,
      "radius" -> robot.shape.radius.asJson,
      "orientation" -> robot.orientation.degrees.asJson,
    )

    val sensorsFields = robot.sensors.toList.map(Encoder[Sensor[?, ?]].apply(_)).asJson

    val actuatorsFields = robot.actuators.toList.map(Encoder[Actuator[?]].apply(_)).asJson

    if robot.sensors.isEmpty && robot.actuators.isEmpty
    then Json.obj(baseFields*)
    else if robot.sensors.isEmpty then
      Json.obj(
        baseFields ++ List("actuators" -> actuatorsFields)*,
      )
    else if robot.actuators.isEmpty then
      Json.obj(
        baseFields ++ List("sensors" -> sensorsFields)*,
      )
    else
      Json.obj(
        baseFields ++ List(
          "sensors" -> sensorsFields,
          "actuators" -> actuatorsFields,
        )*,
      )

end DynamicEntity

export DynamicEntity.given
