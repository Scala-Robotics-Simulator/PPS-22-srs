package io.github.srs.config

/**
 * A trait for decoding configuration values from a map. It provides methods to decode various types of values, such as
 * Int, Long, Double, Boolean, String and List.
 * @tparam A
 *   the type of value to decode
 */
trait Decoder[A]:
  /**
   * Decodes a value from the given field in a configuration map.
   *
   * @param field
   *   the name of the field to decode
   * @param value
   *   the value to decode
   * @return
   *   a `ConfigResult` containing either the decoded value or a sequence of configuration errors
   */
  def decode(field: String, value: Any): ConfigResult[A]

object Decoder:

  given Decoder[Int] with

    def decode(field: String, value: Any): ConfigResult[Int] =
      value match
        case n: Number =>
          if n.intValue() == n.longValue() then Right[Seq[ConfigError], Int](n.intValue())
          else Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int - value out of range")))
        case _ => Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int")))

  given Decoder[Long] with

    def decode(field: String, value: Any): ConfigResult[Long] =
      value match
        case l: Long => Right[Seq[ConfigError], Long](l)
        case n: Number => Right[Seq[ConfigError], Long](n.longValue())
        case _ => Left[Seq[ConfigError], Long](Seq(ConfigError.InvalidType(field, "Long")))

  given Decoder[Double] with

    def decode(field: String, value: Any): ConfigResult[Double] =
      value match
        case d: Double => Right[Seq[ConfigError], Double](d)
        case f: Float => Right[Seq[ConfigError], Double](f.toDouble)
        case i: Int => Right[Seq[ConfigError], Double](i.toDouble)
        case n: Number => Right[Seq[ConfigError], Double](n.doubleValue())
        case _ => Left[Seq[ConfigError], Double](Seq(ConfigError.InvalidType(field, "Double")))

  given Decoder[Boolean] with

    def decode(field: String, value: Any): ConfigResult[Boolean] =
      value match
        case b: Boolean => Right[Seq[ConfigError], Boolean](b)
        case _ => Left[Seq[ConfigError], Boolean](Seq(ConfigError.InvalidType(field, "Boolean")))

  given Decoder[String] with

    def decode(field: String, value: Any): ConfigResult[String] = value match
      case s: String => Right[Seq[ConfigError], String](s)
      case _ => Left[Seq[ConfigError], String](Seq(ConfigError.InvalidType(field, "String")))

  given [A](using decoder: Decoder[A]): Decoder[List[A]] with

    def decode(field: String, value: Any): ConfigResult[List[A]] = value match
      case xs: List[?] =>
        xs.zipWithIndex.foldLeft[ConfigResult[List[A]]](Right[Seq[ConfigError], List[A]](Nil)):
          case (acc, (elem, idx)) =>
            for
              list <- acc
              parsed <- decoder.decode(s"$field[$idx]", elem)
            yield list ++ List(parsed)
      case _ => Left[Seq[ConfigError], List[A]](Seq(ConfigError.InvalidType(field, "List")))

  given [A](using decoder: Decoder[A]): Decoder[Option[A]] with

    def decode(field: String, value: Any): ConfigResult[Option[A]] =
      decoder.decode(field, value).map(Some(_))

  /**
   * Retrieves a value from a map using the specified field name and decodes it using the provided decoder. If the field
   * is not present in the map, it returns a `ConfigError` indicating the missing field.
   * @param field
   *   the name of the field to retrieve
   * @param map
   *   the map containing configuration values
   * @param decoder
   *   the decoder to use for decoding the value
   * @tparam A
   *   the type of value to decode
   * @return
   *   a `ConfigResult` containing either the decoded value or a sequence of configuration errors
   */
  def get[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[A] =
    map
      .get(field)
      .toRight(Seq(ConfigError.MissingField(field)))
      .flatMap(decoder.decode(field, _))

  /**
   * Retrieves an optional value from a map using the specified field name and decodes it using the provided decoder. If
   * the field is not present in the map, it returns `None`.
   * @param field
   *   the name of the field to retrieve
   * @param map
   *   the map containing configuration values
   * @param decoder
   *   decoder to use for decoding the value
   * @tparam A
   *   the type of value to decode
   * @return
   *   a `ConfigResult` containing either the decoded value wrapped in `Some`, or `None` if the field is not present
   */
  def getOptional[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[Option[A]] =
    map.get(field) match
      case Some(value) => decoder.decode(field, value).map(Some(_))
      case None => Right[Seq[ConfigError], Option[A]](None)
end Decoder
