package io.github.srs.config.yaml.decoder.collection

import io.github.srs.config.{ ConfigError, ConfigResult }

import CustomSeq.sequence

@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "scalafix:DisableSyntax.asInstanceOf",
  ),
)
object CustomMap:

  extension (map: Map[String, Any])

    /**
     * Retrieves a sub-map from the given map by key.
     * @param key
     *   the key to look for in the map
     * @return
     *   a `ConfigResult` containing the sub-map. If the key is present and the value is a map, it returns
     *   `Right(subMap)`, if the key is missing it returns `Left(errors)` with a `ConfigError.MissingField`, and if the
     *   value is not a map it returns `Left(errors)` with a `ConfigError.InvalidType`
     */
    def getSubMap(key: String): ConfigResult[Map[String, Any]] =
      map.get(key) match
        case Some(m: Map[?, ?]) => Right[Seq[ConfigError], Map[String, Any]](m.asInstanceOf[Map[String, Any]])
        case Some(_) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.InvalidType(key, "Map[String, Any]")))
        case None => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.MissingField(key)))

    /**
     * Retrieves an optional sub-map from the given map by key.
     * @param key
     *   the key to look for in the map
     * @return
     *   a `ConfigResult` containing an `Option[Map[String, Any]]`. If the key is present it returns
     *   `Right(Some(subMap))`, if the key is missing it returns `Right(None)`, and if there's an error it returns
     *   `Left(errors)`.
     */
    def getOptionalSubMap(key: String): ConfigResult[Option[Map[String, Any]]] =
      map.getSubMap(key) match
        case Right(subMap) => Right[Seq[ConfigError], Option[Map[String, Any]]](Some(subMap))
        case Left(errors) if errors.exists {
              case ConfigError.MissingField(_) => true
              case _ => false
            } =>
          Right[Seq[ConfigError], Option[Map[String, Any]]](None) // If the key is missing, return None
        case Left(errors) =>
          Left[Seq[ConfigError], Option[Map[String, Any]]](errors) // If there's another error, propagate it

    /**
     * Parses a sequence of elements from the map using a provided parsing function.
     * @param key
     *   the key to look for in the map
     * @param parseFunc
     *   a function that takes a `Map[String, Any]` and returns a `ConfigResult[A]`
     * @tparam A
     *   the type of elements in the sequence
     * @return
     *   a `ConfigResult[Seq[A]]` containing the parsed sequence. If the key is present and the value is a list of maps,
     *   it applies the `parseFunc` to each element. If the key is missing, it returns an empty sequence. If the value
     *   is not a list of maps, it returns a `Left` with a `ConfigError.InvalidType`.
     */
    def parseSequence[A](
        key: String,
        parseFunc: Map[String, Any] => ConfigResult[A],
    ): ConfigResult[Seq[A]] =
      map.get(key) match
        case Some(list: List[?]) =>
          val parsed = list.zipWithIndex.map { case (e, i) =>
            e match
              case em: Map[?, ?] =>
                parseFunc(em.asInstanceOf[Map[String, Any]])
              case _ =>
                Left[Seq[ConfigError], A](Seq(ConfigError.InvalidType(s"$key[$i]", "Map[String, Any]")))
          }
          parsed.sequence
        case None => Right[Seq[ConfigError], Seq[A]](Seq.empty)
        case _ => Left[Seq[ConfigError], Seq[A]](Seq(ConfigError.InvalidType(key, "List[Map[String, Any]]")))

  end extension
end CustomMap
