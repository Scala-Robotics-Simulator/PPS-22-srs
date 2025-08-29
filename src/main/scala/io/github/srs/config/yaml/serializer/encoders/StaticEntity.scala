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
      EntityFields.Id -> obs.id.asJson,
      EntityFields.Position -> obs.position.asJson,
      EntityFields.Orientation -> obs.orientation.degrees.asJson,
      ObstacleFields.Width -> obs.width.asJson,
      ObstacleFields.Height -> obs.height.asJson,
    )

  /**
   * Encoder for Light.
   * @return
   *   An Encoder that serializes Light instances to JSON.
   */
  given Encoder[Light] = (light: Light) =>
    Json.obj(
      EntityFields.Id -> light.id.asJson,
      EntityFields.Position -> light.position.asJson,
      EntityFields.Orientation -> light.orientation.degrees.asJson,
      LightFields.Radius -> light.radius.asJson,
      LightFields.IlluminationRadius -> light.illuminationRadius.asJson,
      LightFields.Intensity -> light.intensity.asJson,
      LightFields.Attenuation -> light.attenuation.asJson,
    )
end StaticEntity

export StaticEntity.given
