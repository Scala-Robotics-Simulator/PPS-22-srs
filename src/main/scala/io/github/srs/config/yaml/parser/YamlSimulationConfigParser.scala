package io.github.srs.config.yaml.parser

import io.github.srs.config.yaml.parser.collection.CustomMap.*
import io.github.srs.config.{ ConfigError, ConfigResult, SimulationConfig }
import io.github.srs.model.Simulation
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.Actuator
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.sensor.dsl.ProximitySensorDsl.*
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

      simMap <- root.getOptionalSubMap("simulation")
      envMap <- root.getOptionalSubMap("environment")

      sim <- parseSimulation(simMap)
      env <- parseEnvironment(envMap)
    yield SimulationConfig(sim, env)

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
          entities <- m.parseSequence("entities", parseEntity)
        yield Environment()
          |> (env => width.fold(env)(env.withWidth))
          |> (env => height.fold(env)(env.withHeight))
          |> (_.containing(entities.toSet))
        end for
    end match

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
    // TODO: Add support for robot behavior
    for
      pos <- get[List[Int]]("position", map)
      orient <- getOptional[Double]("orientation", map)
      radius <- getOptional[Double]("radius", map)
      speed <- getOptional[Double]("speed", map)
      prox <- getOptional[Boolean]("withProximitySensors", map)
      light <- getOptional[Boolean]("withLightSensors", map)
      actuators <- map.parseSequence("actuators", parseActuator)
      sensors <- map.parseSequence("sensors", parseSensor)
    yield Robot().at(Point2D(pos.head, pos(1)))
      |> (r => orient.fold(r)(o => r.withOrientation(Orientation(o))))
      |> (r => radius.fold(r)(radius => r.withShape(ShapeType.Circle(radius))))
      |> (r => speed.fold(r)(s => r.withSpeed(s)))
      |> (r => if prox.getOrElse(false) then r.withProximitySensors else r)
      |> (r => if light.getOrElse(false) then r.withLightSensors else r)
      |> (r => if actuators.nonEmpty then r.withActuators(actuators) else r)
      |> (r => if sensors.nonEmpty then r.withSensors(sensors) else r)

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
   * Parses an actuator from the given map.
   * @param map
   *   the map containing actuator parameters
   * @return
   *   a `ConfigResult` containing the parsed `Actuator[Robot]` or the errors encountered during parsing.
   */
  private def parseActuator(map: Map[String, Any]): ConfigResult[Actuator[Robot]] =
    map.headOption match
      case Some(("differentialWheelMotor", v: Map[?, ?])) =>
        parseDifferentialWheelMotor(v.asInstanceOf[Map[String, Any]])
      case Some((key, _)) =>
        Left[Seq[ConfigError], Actuator[Robot]](Seq(ConfigError.ParsingError(s"Unknown actuator type: $key")))
      case None => Left[Seq[ConfigError], Actuator[Robot]](Seq(ConfigError.ParsingError("Empty actuator map")))

  /**
   * Parses a [[io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor]] from the given map.
   * @param map
   *   the map containing differential wheel motor parameters
   * @return
   *   a `ConfigResult` containing the parsed `Actuator[Robot]` or the errors encountered during parsing.
   */
  private def parseDifferentialWheelMotor(map: Map[String, Any]): ConfigResult[Actuator[Robot]] =
    for
      leftSpeed <- get[Double]("leftSpeed", map)
      rightSpeed <- get[Double]("rightSpeed", map)
    yield differentialWheelMotor withLeftSpeed leftSpeed withRightSpeed rightSpeed

  /**
   * Parses a [[Sensor]] from the given map.
   * @param map
   *   the map containing sensor parameters
   * @return
   *   a `ConfigResult` containing the parsed `Sensor[Robot, Environment]` or the errors encountered during parsing.
   */
  private def parseSensor(map: Map[String, Any]): ConfigResult[Sensor[Robot, Environment]] =
    map.headOption match
      case Some(("proximitySensor", v: Map[?, ?])) =>
        parseProximitySensor(v.asInstanceOf[Map[String, Any]])
      case Some((key, _)) =>
        Left[Seq[ConfigError], Sensor[Robot, Environment]](Seq(ConfigError.ParsingError(s"Unknown sensor type: $key")))
      case None => Left[Seq[ConfigError], Sensor[Robot, Environment]](Seq(ConfigError.ParsingError("Empty sensor map")))

  /**
   * Parses a [[io.github.srs.model.entity.dynamicentity.sensor.ProximitySensor]] from the given map.
   * @param map
   *   the map containing proximity sensor parameters
   * @return
   *   a `ConfigResult` containing the parsed `Sensor[Robot, Environment]` or the errors encountered during parsing.
   */
  private def parseProximitySensor(map: Map[String, Any]): ConfigResult[Sensor[Robot, Environment]] =
    for
      offset <- get[Double]("offset", map)
      distance <- get[Double]("distance", map)
      range <- get[Double]("range", map)
    yield proximitySensor withOffset Orientation(offset) withDistance distance withRange range

end YamlSimulationConfigParser
