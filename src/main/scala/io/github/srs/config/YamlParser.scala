package io.github.srs.config

import cats.effect.Sync

object YamlParser:

  def parse[F[_]: Sync](content: String): F[ConfigResult[SimulationConfig]] =
    Sync[F].pure(YamlParsers.parseSimulationConfig(content))
