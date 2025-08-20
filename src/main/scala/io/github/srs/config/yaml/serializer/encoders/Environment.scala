package io.github.srs.config.yaml.serializer.encoders

import scala.language.postfixOps

import io.circe.syntax.*
import io.circe.{ Encoder, Json }
import io.github.srs.config.yaml.serializer.encoders.given
import io.github.srs.model.entity.staticentity.StaticEntity.Boundary
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults.Fields.Environment as EnvironmentFields

/**
 * Encodes an [[Environment]] to a JSON object.
 */
object Environment:

  /**
   * Given an [[Environment]], it encodes it to a JSON object.
   *
   * @return
   *   a JSON object representing the environment
   */
  given Encoder[Environment] = (environment: Environment) =>
    val baseFields = List(
      EnvironmentFields.width -> environment.width.asJson,
      EnvironmentFields.height -> environment.height.asJson,
    )
    val entitiesFields = environment.entities.filterNot {
      case Boundary(_, _, _, _, _) =>
        true // Boundary entities are not serialized
      case _ => false
    }.toList.map(_.asJson).asJson
    if environment.entities.isEmpty then Json.obj(baseFields*)
    else
      Json.obj(
        baseFields ++ List(EnvironmentFields.entities -> entitiesFields)*,
      )
end Environment

export Environment.given
