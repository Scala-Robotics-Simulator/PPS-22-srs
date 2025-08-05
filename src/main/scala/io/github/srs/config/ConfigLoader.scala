package io.github.srs.config

import cats.syntax.all.*
import cats.effect.Sync
import fs2.io.file.{ Files, Path }
import fs2.text

trait ConfigLoader[F[_]]:
  def load: F[ConfigResult[SimulationConfig]]

final case class YamlConfigLoader[F[_]: {Files, Sync}](path: Path) extends ConfigLoader[F]:

  override def load: F[ConfigResult[SimulationConfig]] =
    for
      content <- Files[F].readAll(path).through(text.utf8.decode).compile.string
      config <- YamlParser.parse[F](content)
    yield config

enum ConfigResult[+A]:
  case Success(config: A)
  case Error(errors: Seq[ConfigError])

enum ConfigError:
  case InvalidFile(message: String)
  case InvalidVersion(version: String)
  case MissingField(field: String)
  case InvalidField(field: String, reason: String)
  case ParsingError(message: String)
