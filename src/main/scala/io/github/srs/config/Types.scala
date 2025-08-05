package io.github.srs.config

type ConfigResult[+A] = Either[Seq[ConfigError], A]

enum ConfigError:
  case MissingField(field: String)
  case ParsingError(message: String)
  case InvalidType(field: String, expected: String)
