package io.github.srs.config

trait Decoder[A]:
  def decode(field: String, value: Any): ConfigResult[A]

object Decoder:
  def apply[A](using dec: Decoder[A]): Decoder[A] = dec

  given Decoder[Int] with

    def decode(field: String, value: Any): ConfigResult[Int] = value match
      case i: Int => Right[Seq[ConfigError], Int](i)
      case _ => Left[Seq[ConfigError], Int](Seq(ConfigError.InvalidType(field, "Int")))

  given Decoder[Long] with

    def decode(field: String, value: Any): ConfigResult[Long] = value match
      case l: Long => Right[Seq[ConfigError], Long](l)
      case i: Int => Right[Seq[ConfigError], Long](i.toLong)
      case _ => Left[Seq[ConfigError], Long](Seq(ConfigError.InvalidType(field, "Long")))

  given Decoder[Double] with

    def decode(field: String, value: Any): ConfigResult[Double] = value match
      case d: Double => Right[Seq[ConfigError], Double](d)
      case f: Float => Right[Seq[ConfigError], Double](f.toDouble)
      case i: Int => Right[Seq[ConfigError], Double](i.toDouble)
      case _ => Left[Seq[ConfigError], Double](Seq(ConfigError.InvalidType(field, "Double")))

  given Decoder[Boolean] with

    def decode(field: String, value: Any): ConfigResult[Boolean] = value match
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

  def get[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[A] =
    map
      .get(field)
      .toRight(Seq(ConfigError.MissingField(field)))
      .flatMap(decoder.decode(field, _))

  def getOptional[A](field: String, map: Map[String, Any])(using decoder: Decoder[A]): ConfigResult[Option[A]] =
    map.get(field) match
      case Some(value) => decoder.decode(field, value).map(Some(_))
      case None => Right[Seq[ConfigError], Option[A]](None)
end Decoder
