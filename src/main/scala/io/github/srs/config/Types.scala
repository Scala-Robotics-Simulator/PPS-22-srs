package io.github.srs.config

/**
 * A type alias for the result of a configuration decoding operation. It represents either a successful decoding with a
 * value of type `A` or a sequence of configuration errors.
 */
type ConfigResult[+A] = Either[Seq[ConfigError], A]

/**
 * An enumeration of possible configuration errors that can occur during the decoding of configuration values.
 */
enum ConfigError:
  /**
   * Represents an error where a required field is missing in the configuration.
   * @param field
   *   the name of the missing field
   */
  case MissingField(field: String)

  /**
   * Represents an error that occurs during parsing of the configuration.
   * @param message
   *   a message describing the parsing error
   */
  case ParsingError(message: String)

  /**
   * Represents an error where a value is of an invalid type.
   * @param field
   *   the name of the field with the invalid type
   * @param expected
   *   a description of the expected type
   */
  case InvalidType(field: String, expected: String)
end ConfigError
