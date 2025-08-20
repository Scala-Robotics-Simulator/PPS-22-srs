package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.entity.staticentity.StaticEntity.{ Light, Obstacle }
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields

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
      EntityFields.id -> obs.id.asJson,
      EntityFields.position -> obs.position.asJson,
      EntityFields.orientation -> obs.orientation.degrees.asJson,
      ObstacleFields.width -> obs.width.asJson,
      ObstacleFields.height -> obs.height.asJson,
    )

  /**
   * Encoder for Light.
   * @return
   *   An Encoder that serializes Light instances to JSON.
   */
  given Encoder[Light] = (light: Light) =>
    Json.obj(
      EntityFields.id -> light.id.asJson,
      EntityFields.position -> light.position.asJson,
      EntityFields.orientation -> light.orientation.degrees.asJson,
      LightFields.radius -> light.radius.asJson,
      LightFields.illuminationRadius -> light.illuminationRadius.asJson,
      LightFields.intensity -> light.intensity.asJson,
      LightFields.attenuation -> light.attenuation.asJson,
    )
end StaticEntity

export StaticEntity.given
