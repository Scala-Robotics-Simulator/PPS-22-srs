package io.github.srs.config.yaml.parser

import java.util.UUID

import io.github.srs.config.yaml.parser.collection.CustomMap.*
import io.github.srs.config.{ ConfigError, ConfigResult, SimulationConfig }
import io.github.srs.model.Simulation
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.chaining.Pipe.given
import org.virtuslab.yaml.*
import io.github.srs.utils.SimulationDefaults.Fields.Simulation as SimulationFields
import io.github.srs.utils.SimulationDefaults.Fields.Environment as EnvironmentFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity as EntityFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Agent as AgentFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.robot.behavior.Policy
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.reward.Reward
import io.github.srs.model.entity.dynamicentity.agent.termination.Termination
import io.github.srs.model.entity.dynamicentity.agent.truncation.Truncation
import io.github.srs.model.entity.dynamicentity.agent.dsl.AgentDsl.*
import io.github.srs.model.environment.Environment

/**
 * A parser for YAML configuration files, specifically for simulation configurations.
 */
object YamlSimulationConfigParser:

  import Decoder.{ get, getOptional, given }

  /**
   * Parses a YAML configuration string into a `SimulationConfig`.
   * @param content
   *   the YAML configuration content as a string
   * @return
   *   a `ConfigResult` containing the parsed `SimulationConfig`
   */
  def parseSimulationConfig(content: String): ConfigResult[SimulationConfig[Environment]] =
    for
      root <- content.as[Map[String, Any]] match
        case Left(err) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.ParsingError(err.getMessage)))
        case Right(map) => Right[Seq[ConfigError], Map[String, Any]](map)

      simMap <- root.getOptionalSubMap(SimulationFields.Self)
      envMap <- root.getOptionalSubMap(EnvironmentFields.Self)

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
          duration <- getOptional[Long](SimulationFields.Duration, m)
          seed <- getOptional[Long](SimulationFields.Seed, m)
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
          width <- getOptional[Int](EnvironmentFields.Width, m)
          height <- getOptional[Int](EnvironmentFields.Height, m)
          entities <- m.parseSequence(EnvironmentFields.Entities, parseEntity)
        yield Environment()
          |> (env => width.fold(env)(env.withWidth))
          |> (env => height.fold(env)(env.withHeight))
          |> (_.containing(entities.toList))
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
      case Some((ObstacleFields.Self, v: Map[String, Any] @unchecked)) => parseObstacle(v)
      case Some((LightFields.Self, v: Map[String, Any] @unchecked)) => parseLight(v)
      case Some((RobotFields.Self, v: Map[String, Any] @unchecked)) => YamlSimulationConfigParser.parseRobot(v)
      case Some((AgentFields.Self, v: Map[String, Any] @unchecked)) => YamlSimulationConfigParser.parseAgent(v)
      case Some((key, _)) => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError(s"Unknown entity type: $key")))
      case None => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError("Empty entity map")))

  private def parsePosition(pos: List[Double]): ConfigResult[Point2D] =
    pos match
      case x :: y :: Nil => Right[Seq[ConfigError], Point2D]((x, y))
      case _ =>
        Left[Seq[ConfigError], Point2D](Seq(ConfigError.ParsingError("Position must be in the format: `[x, y]`")))

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
    for
      id <- getOptional[UUID](EntityFields.Id, map)
      pos <- get[List[Double]](EntityFields.Position, map)
      position <- parsePosition(pos)
      orient <- getOptional[Double](EntityFields.Orientation, map)
      radius <- getOptional[Double](RobotFields.Radius, map)
      speed <- getOptional[Double](RobotFields.Speed, map)
      prox <- getOptional[Boolean](RobotFields.WithProximitySensors, map)
      light <- getOptional[Boolean](RobotFields.WithLightSensors, map)
      behavior <- getOptional[Policy](RobotFields.Behavior, map)
    yield Robot().at(position)
      |> (r => id.fold(r)(r.withId))
      |> (r => orient.fold(r)(o => r.withOrientation(Orientation(o))))
      |> (r => radius.fold(r)(radius => r.withShape(ShapeType.Circle(radius))))
      |> (r => speed.fold(r)(s => r.withSpeed(s)))
      |> (r => if prox.getOrElse(false) then r.withProximitySensors else r)
      |> (r => if light.getOrElse(false) then r.withLightSensors else r)
      |> (r => behavior.fold(r)(b => r.withBehavior(b)))

  /**
   * Parses an agent entity from the given map.
   * @note
   *   This method currently does not support custom reward models and wheel actuators beyond speed configuration.
   * @param map
   *   the map containing agent parameters
   * @return
   *   a `ConfigResult` containing the parsed `Agent` or the errors encountered during parsing.
   */
  private def parseAgent(map: Map[String, Any]): ConfigResult[Entity] =
    for
      id <- getOptional[UUID](EntityFields.Id, map)
      pos <- get[List[Double]](EntityFields.Position, map)
      position <- parsePosition(pos)
      orient <- getOptional[Double](EntityFields.Orientation, map)
      radius <- getOptional[Double](AgentFields.Radius, map)
      speed <- getOptional[Double](AgentFields.Speed, map)
      prox <- getOptional[Boolean](AgentFields.WithProximitySensors, map)
      light <- getOptional[Boolean](AgentFields.WithLightSensors, map)
      reward <- getOptional[Reward](AgentFields.Reward, map)
      termination <- getOptional[Termination](AgentFields.Termination, map)
      truncation <- getOptional[Truncation](AgentFields.Truncation, map)
    yield Agent().at(position)
      |> (a => id.fold(a)(a.withId))
      |> (a => orient.fold(a)(o => a.withOrientation(Orientation(o))))
      |> (a => radius.fold(a)(radius => a.withShape(ShapeType.Circle(radius))))
      |> (a => speed.fold(a)(s => a.withSpeed(s)))
      |> (a => if prox.getOrElse(false) then a.withProximitySensors else a)
      |> (a => if light.getOrElse(false) then a.withLightSensors else a)
      |> (a => reward.fold(a)(r => a.withReward(r.toRewardModel)))
      |> (a => termination.fold(a)(t => a.withTermination(t.toTerminationModel)))
      |> (a => truncation.fold(a)(t => a.withTruncation(t.toTruncationModel)))

  /**
   * Parses an obstacle entity from the given map.
   * @param map
   *   the map containing obstacle parameters
   * @return
   *   a `ConfigResult` containing the parsed `Entity` or the errors encountered during parsing.
   */
  private def parseObstacle(map: Map[String, Any]): ConfigResult[Entity] =
    for
      id <- getOptional[UUID](EntityFields.Id, map)
      pos <- get[List[Double]](EntityFields.Position, map)
      position <- parsePosition(pos)
      orientation <- getOptional[Double](EntityFields.Orientation, map)
      width <- getOptional[Double](ObstacleFields.Width, map)
      height <- getOptional[Double](ObstacleFields.Height, map)
    yield obstacle.at(position)
      |> (obs => id.fold(obs)(obs.withId))
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
      id <- getOptional[UUID](EntityFields.Id, map)
      pos <- get[List[Double]](EntityFields.Position, map)
      position <- parsePosition(pos)
      orientation <- getOptional[Double](EntityFields.Orientation, map)
      radius <- getOptional[Double](LightFields.Radius, map)
      illumination <- get[Double](LightFields.IlluminationRadius, map)
      intensity <- getOptional[Double](LightFields.Intensity, map)
      attenuation <- getOptional[Double](LightFields.Attenuation, map)
    yield light.at(position).withIlluminationRadius(illumination)
      |> (l => id.fold(l)(l.withId))
      |> (l => orientation.fold(l)(o => l.withOrientation(Orientation(o))))
      |> (l => radius.fold(l)(r => l.withRadius(r)))
      |> (l => intensity.fold(l)(i => l.withIntensity(i)))
      |> (l => attenuation.fold(l)(a => l.withAttenuation(a)))

end YamlSimulationConfigParser
