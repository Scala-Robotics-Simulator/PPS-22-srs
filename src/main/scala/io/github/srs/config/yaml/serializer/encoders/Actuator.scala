package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }

/**
 * Encoders for Actuator types.
 */
object Actuator:

  /**
   * Encoder for Actuator types.
   * @return
   *   An Encoder that serializes Actuator instances to JSON.
   */
  given Encoder[Actuator[?]] = { case dwm: DifferentialWheelMotor =>
    Json.obj("differentialWheelMotor" -> Encoder[DifferentialWheelMotor].apply(dwm))
  }

  /**
   * Encoder for DifferentialWheelMotor.
   * @return
   *   An Encoder that serializes DifferentialWheelMotor instances to JSON.
   */
  given Encoder[DifferentialWheelMotor] = (dwm: DifferentialWheelMotor) =>
    Json.obj(
      "leftSpeed" -> dwm.left.speed.asJson,
      "rightSpeed" -> dwm.right.speed.asJson,
    )
end Actuator

export Actuator.given
