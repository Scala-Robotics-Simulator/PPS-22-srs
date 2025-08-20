package io.github.srs.config.yaml.serializer.encoders

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.model.entity.Entity
import io.github.srs.model.entity.dynamicentity.{ DynamicEntity, Robot }
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields

/**
 * Encoders for various entity types.
 */
object Entity:

  /**
   * Encoder for a position represented as a tuple of (Double, Double).
   * @return
   *   An Encoder that serializes a position to JSON.
   */
  given Encoder[(Double, Double)] = (position: (Double, Double)) => s"[${position._1}, ${position._2}]".asJson

  /**
   * Encoder for Entity types.
   * @return
   */
  given Encoder[Entity] =
    case e: DynamicEntity =>
      e match
        case r: Robot =>
          Json.obj(RobotFields.self -> r.asJson)
    case e: StaticEntity =>
      e match
        case o: StaticEntity.Obstacle =>
          Json.obj(ObstacleFields.self -> o.asJson)
        case l: StaticEntity.Light =>
          Json.obj(LightFields.self -> l.asJson)
        case _: StaticEntity.Boundary =>
          Json.obj() // Boundary entities are not serialized
end Entity

export Entity.given
