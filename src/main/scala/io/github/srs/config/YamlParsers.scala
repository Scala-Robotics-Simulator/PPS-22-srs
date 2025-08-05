package io.github.srs.config

import io.github.srs.config.ConfigResult
import io.github.srs.model.Simulation
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.*
import io.github.srs.model.environment.Environment
import org.virtuslab.yaml.*

@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "org.wartremover.warts.IterableOps",
    "org.wartremover.warts.SeqApply",
    "scalafix:DisableSyntax.asInstanceOf",
  ),
)
object YamlParsers:

  import Decoder.{ get, given }

  def parseSimulationConfig(content: String): ConfigResult[SimulationConfig] =
    for
      root <- content.as[Map[String, Any]] match
        case Left(err) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.ParsingError(err.getMessage)))
        case Right(map) => Right[Seq[ConfigError], Map[String, Any]](map)

      simMap <- getSubMap("simulation", root)
      envMap <- getSubMap("environment", root)

      sim <- parseSimulation(simMap)
      env <- parseEnvironment(envMap)
    yield SimulationConfig(sim, env)

  private def getSubMap(key: String, map: Map[String, Any]): ConfigResult[Map[String, Any]] =
    map.get(key) match
      case Some(m: Map[?, ?]) => Right[Seq[ConfigError], Map[String, Any]](m.asInstanceOf[Map[String, Any]])
      case Some(_) => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.InvalidType(key, "Map[String, Any]")))
      case None => Left[Seq[ConfigError], Map[String, Any]](Seq(ConfigError.MissingField(key)))

  private def parseSimulation(map: Map[String, Any]): ConfigResult[Simulation] =
    for
      duration <- get[Int]("duration", map)
      seed <- get[Long]("seed", map)
    yield Simulation.simulation.withDuration(duration).withSeed(seed)

  private def parseEnvironment(map: Map[String, Any]): ConfigResult[Environment] =
    import io.github.srs.model.environment.dsl.CreationDSL.*
    for
      width <- get[Int]("width", map)
      height <- get[Int]("height", map)
      entities <- map.get("entities") match
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
      .withWidth(width)
      .withHeight(height)
      .containing(entities)
    end for

  end parseEnvironment

  private def parseEntity(map: Map[String, Any]): ConfigResult[Entity] =
    map.headOption match
      case Some(("obstacle", v: Map[?, ?])) => parseObstacle(v.asInstanceOf[Map[String, Any]])
      case Some(("light", v: Map[?, ?])) => parseLight(v.asInstanceOf[Map[String, Any]])
      case Some(("robot", v: Map[?, ?])) => YamlParsers.parseRobot(v.asInstanceOf[Map[String, Any]])
      case Some((key, _)) => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError(s"Unknown entity type: $key")))
      case None => Left[Seq[ConfigError], Entity](Seq(ConfigError.ParsingError("Empty entity map")))

  private def parseRobot(map: Map[String, Any]): ConfigResult[Entity] =
    import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
    for
      pos <- get[List[Int]]("position", map)
      orient <- get[Double]("orientation", map)
      radius <- get[Double]("radius", map)
      speed <- get[Double]("speed", map)
      prox <- get[Boolean]("withProximitySensors", map)
      light <- get[Boolean]("withLightSensors", map)
    yield
      val robot = Robot()
        .at(Point2D(pos.head, pos(1)))
        .withOrientation(Orientation(orient))
        .withShape(ShapeType.Circle(radius))
        .withSpeed(speed)

      val robotAndMaybeProximity = if prox then robot.withProximitySensors else robot
      val robotAndMaybeBoth = if light then robot.withLightSensors else robotAndMaybeProximity
      robotAndMaybeBoth

  private def parseObstacle(map: Map[String, Any]): ConfigResult[Entity] =
    import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
    for
      pos <- get[List[Int]]("position", map)
      orientation <- get[Double]("orientation", map)
      width <- get[Double]("width", map)
      height <- get[Double]("height", map)
    yield obstacle
      .at(Point2D(pos.head, pos(1)))
      .withOrientation(Orientation(orientation))
      .withWidth(width)
      .withHeight(height)

  private def parseLight(map: Map[String, Any]): ConfigResult[Entity] =
    import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
    for
      pos <- get[List[Int]]("position", map)
      radius <- get[Double]("illuminationRadius", map)
      intensity <- get[Double]("intensity", map)
      attenuation <- get[Double]("attenuation", map)
    yield light
      .at(Point2D(pos.head, pos(1)))
      .withIlluminationRadius(radius)
      .withIntensity(intensity)
      .withAttenuation(attenuation)

  private def sequence[A](results: Seq[ConfigResult[A]]): ConfigResult[Seq[A]] =
    results.foldRight(Right[Seq[ConfigError], Seq[A]](Nil): ConfigResult[Seq[A]]):
      case (Right(a), Right(as)) => Right[Seq[ConfigError], Seq[A]](a +: as)
      case (Left(e1), Right(_)) => Left[Seq[ConfigError], Seq[A]](e1)
      case (Right(_), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e2)
      case (Left(e1), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e1 ++ e2)

end YamlParsers
