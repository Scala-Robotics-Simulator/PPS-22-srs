package io.github.srs.config.yaml.serializer.encoders

import io.github.srs.model.environment.Environment

object Environment:

  import io.circe.syntax.*
  import io.circe.{ Encoder, Json }

  given Encoder[Environment] = (environment: Environment) =>
    Json.obj(
      "width" -> environment.width.asJson,
      "height" -> environment.height.asJson,
    )

export Environment.given
