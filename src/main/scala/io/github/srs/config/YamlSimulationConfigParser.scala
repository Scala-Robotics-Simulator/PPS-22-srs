package io.github.srs.config

import io.github.srs.config.ConfigResult
import io.github.srs.model.Simulation
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.chaining.Pipe.given
import org.virtuslab.yaml.*

/**
 * A parser for YAML configuration files, specifically for simulation configurations.
 */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "org.wartremover.warts.IterableOps",
    "org.wartremover.warts.SeqApply",
    "scalafix:DisableSyntax.asInstanceOf",
  ),
)
object YamlSimulationConfigParser:

  import Decoder.{ get, getOptional, given }

  /**
   * Parses a YAML configuration string into a `SimulationConfig`.
   * @param content
   *   the YAML configuration content as a string
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  def parseSimulationConfig(content: String): ConfigResult[SimulationConfig] =
    for
      root <- content.as[Map[String, Any]] match
        case Left(err) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.ParsingError(err.getMessage)))
        case Right(map) => Right[Seq[ConfigError], Map[String, Any]](map)

      simMap <- getOptionalSubMap("simulation", root)
      envMap <- getOptionalSubMap("environment", root)

      sim <- parseSimulation(simMap)
      env <- parseEnvironment(envMap)
    yield SimulationConfig(sim, env)

  /**
   * Retrieves an optional sub-map from the given map by key.
   * @param key
   *   the key to look for in the map
   * @param map
   *   the map from which to retrieve the sub-map
   * @return
   *   a `ConfigResult` containing an `Option[Map[String, Any]]`. If the key is present it returns
   *   `Right(Some(subMap))`, if the key is missing it returns `Right(None)`, and if there's an error it returns
   *   `Left(errors)`.
   */
  private def getOptionalSubMap(key: String, map: Map[String, Any]): ConfigResult[Option[Map[String, Any]]] =
    getSubMap(key, map) match
      case Right(subMap) => Right[Seq[ConfigError], Option[Map[String, Any]]](Some(subMap))
      case Left(errors) if errors.exists {
            case ConfigError.MissingField(_) => true
            case _ => false
          } =>
        Right[Seq[ConfigError], Option[Map[String, Any]]](None) // If the key is missing, return None
      case Left(errors) =>
        Left[Seq[ConfigError], Option[Map[String, Any]]](errors) // If there's another error, propagate it

  /**
   * Retrieves a sub-map from the given map by key.
   * @param key
   *   the key to look for in the map
   * @param map
   *   the map from which to retrieve the sub-map
   * @return
   *   a `ConfigResult` containing the sub-map. If the key is present and the value is a map, it returns
   *   `Right(subMap)`, if the key is missing it returns `Left(errors)` with a `ConfigError.MissingField`, and if the
   *   value is not a map it returns `Left(errors)` with a `ConfigError.InvalidType`
   */
  private def getSubMap(key: String, map: Map[String, Any]): ConfigResult[Map[String, Any]] =
    map.get(key) match
      case Some(m: Map[?, ?]) => Right[Seq[ConfigError], Map[String, Any]](m.asInstanceOf[Map[String, Any]])
      case Some(_) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.InvalidType(key, "Map[String, Any]")))
      case None => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.MissingField(key)))

  /**
   * Parses the simulation portion of the configuration.
   * @param map
   *   the optional map containing simulation parameters
   * @return
   *   a `ConfigResult` containing the parsed `Simulation` or the default simulation if the map is `None`.
   */
  private def parseSimulation(map: Option[Map[String, Any]]): ConfigResult[Simulation] =
    map match
      case None => Right[Seq[ConfigError], Simulation](Simulation.simulation)
      case Some(m) =>
        for
          duration <- getOptional[Int]("duration", m)
          seed <- getOptional[Long]("seed", m)
        yield Simulation.simulation
          |> (sim => duration.fold(sim)(sim.withDuration))
          |> (sim => seed.fold(sim)(sim.withSeed))

  /**
   * Parses the environment portion of the configuration.
   * @param map
   *   the optional map containing environment parameters
   * @return
   *   a `ConfigResult` containing the parsed `Environment` or the default environment if the map is `None`.
   */
  private def parseEnvironment(map: Option[Map[String, Any]]): ConfigResult[Environment] =
    map match
      case None => Right[Seq[ConfigError], Environment](environment)
      case Some(m) =>
        for
          width <- getOptional[Int]("width", m)
          height <- getOptional[Int]("height", m)
          entities <- m.get("entities") match
            case Some(list: List[?]) =>
              val parsed = list.zipWithIndex.map { case (e, i) =>
                e match
                  case em: Map[?, ?] =>
                    parseEntity(em.asInstanceOf[Map[String, Any]])
                  case _ =>
                    Left[Seq[ConfigError], Entity](Seq(ConfigError.InvalidType(s"entities[$i]", "Map[String, Any]")))
              }
              sequence(parsed).map(_.toSet)
            case None => Right[Seq[ConfigError], Set[Entity]](Set.empty)
            case _ =>
              Left[Seq[ConfigError], Set[Entity]](Seq(ConfigError.InvalidType("entities", "List[Map[String, Any]]")))
        yield Environment()
          |> (env => width.fold(env)(env.withWidth))
          |> (env => height.fold(env)(env.withHeight))
          |> (_.containing(entities))
        end for
    end match

  end parseEnvironment

  /**
   * Parses an entity from the given map.
   * @param map
   *   the map containing entity parameters
   * @return
   *   a `ConfigResult` containing the parsed `Entity`. If the entity type is recognized, it returns `Right(entity)`, if
   *   the entity type is unknown or if the map is empty, it returns `Left(errors)` with a `ConfigError.ParsingError`.
   */
  private def parseEntity(map: Map[String, Any]): ConfigResult[Entity] =
    map.headOption match
      case Some(("obstacle", v: Map[?, ?])) => parseObstacle(v.asInstanceOf[Map[String, Any]])
      case Some(("light", v: Map[?, ?])) => parseLight(v.asInstanceOf[Map[String, Any]])
      case Some(("robot", v: Map[?, ?])) => YamlSimulationConfigParser.parseRobot(v.asInstanceOf[Map[String, Any]])
      case Some((key, _)) => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError(s"Unknown entity type: $key")))
      case None => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError("Empty entity map")))

  /**
   * Parses a robot entity from the given map.
   * @note
   *   This method currently does not support robot behavior configuration and wheel actuators.
   * @param map
   *   the map containing robot parameters
   * @return
   *   a `ConfigResult` containing the parsed `Robot` or the errors encountered during parsing.
   */
  private def parseRobot(map: Map[String, Any]): ConfigResult[Entity] =
    // TODO: Add support for robot behavior and wheel actuators
    for
      pos <- get[List[Int]]("position", map)
      orient <- getOptional[Double]("orientation", map)
      radius <- getOptional[Double]("radius", map)
      speed <- getOptional[Double]("speed", map)
      prox <- getOptional[Boolean]("withProximitySensors", map)
      light <- getOptional[Boolean]("withLightSensors", map)
    yield Robot().at(Point2D(pos.head, pos(1)))
      |> (r => orient.fold(r)(o => r.withOrientation(Orientation(o))))
      |> (r => radius.fold(r)(radius => r.withShape(ShapeType.Circle(radius))))
      |> (r => speed.fold(r)(s => r.withSpeed(s)))
      |> (r => if prox.getOrElse(false) then r.withProximitySensors else r)
      |> (r => if light.getOrElse(false) then r.withLightSensors else r)

  /**
   * Parses an obstacle entity from the given map.
   * @param map
   *   the map containing obstacle parameters
   * @return
   *   a `ConfigResult` containing the parsed `Entity` or the errors encountered during parsing.
   */
  private def parseObstacle(map: Map[String, Any]): ConfigResult[Entity] =
    for
      pos <- get[List[Int]]("position", map)
      orientation <- getOptional[Double]("orientation", map)
      width <- getOptional[Double]("width", map)
      height <- getOptional[Double]("height", map)
    yield obstacle.at(Point2D(pos.head, pos(1)))
      |> (obs => orientation.fold(obs)(o => obs.withOrientation(Orientation(o))))
      |> (obs => width.fold(obs)(w => obs.withWidth(w)))
      |> (obs => height.fold(obs)(h => obs.withHeight(h)))

  /**
   * Parses a light entity from the given map.
   * @param map
   *   the map containing light parameters
   * @return
   *   a `ConfigResult` containing the parsed `Entity` or the errors encountered during parsing.
   */
  private def parseLight(map: Map[String, Any]): ConfigResult[Entity] =
    for
      pos <- get[List[Int]]("position", map)
      radius <- get[Double]("illuminationRadius", map)
      intensity <- getOptional[Double]("intensity", map)
      attenuation <- getOptional[Double]("attenuation", map)
    yield light.at(Point2D(pos.head, pos(1))).withIlluminationRadius(radius)
      |> (l => intensity.fold(l)(i => l.withIntensity(i)))
      |> (l => attenuation.fold(l)(a => l.withAttenuation(a)))

  /**
   * Utility method to sequence a collection of `ConfigResult`s into a single `ConfigResult` containing a sequence of
   * results.
   * @param results
   *   the sequence of `ConfigResult`s to be sequenced
   * @tparam A
   *   the type of the results contained in the `ConfigResult`
   * @return
   *   a `ConfigResult` containing a sequence of results if all are successful, or a sequence of errors if any fail.
   */
  private def sequence[A](results: Seq[ConfigResult[A]]): ConfigResult[Seq[A]] =
    results.foldRight(Right[Seq[ConfigError], Seq[A]](Nil): ConfigResult[Seq[A]]):
      case (Right(a), Right(as)) => Right[Seq[ConfigError], Seq[A]](a +: as)
      case (Left(e1), Right(_)) => Left[Seq[ConfigError], Seq[A]](e1)
      case (Right(_), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e2)
      case (Left(e1), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e1 ++ e2)

end YamlSimulationConfigParser
