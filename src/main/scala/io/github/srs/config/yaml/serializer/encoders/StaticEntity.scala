package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }

/**
 * Encoders for StaticEntity types.
 */
object StaticEntity:

  /**
   * Encoder for StaticEntity types.
   * @return
   *   An Encoder that serializes StaticEntity instances to JSON.
   */
  given Encoder[Obstacle] = (obs: Obstacle) =>
    Json.obj(
      "id" -> obs.id.asJson,
      "position" -> obs.position.asJson,
      "orientation" -> obs.orientation.degrees.asJson,
      "width" -> obs.width.asJson,
      "height" -> obs.height.asJson,
    )

  /**
   * Encoder for Light.
   * @return
   *   An Encoder that serializes Light instances to JSON.
   */
  given Encoder[Light] = (light: Light) =>
    Json.obj(
      "id" -> light.id.asJson,
      "position" -> light.position.asJson,
      "orientation" -> light.orientation.degrees.asJson,
      "radius" -> light.radius.asJson,
      "illuminationRadius" -> light.illuminationRadius.asJson,
      "intensity" -> light.intensity.asJson,
      "attenuation" -> light.attenuation.asJson,
    )
end StaticEntity

export StaticEntity.given
