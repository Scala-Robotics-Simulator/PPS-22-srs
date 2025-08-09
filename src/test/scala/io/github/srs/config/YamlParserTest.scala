package io.github.srs.config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.{ Files, Path }
import fs2.text
import io.github.srs.config.ConfigError.MissingField
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.sensor.ProximitySensor
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class YamlParserTest extends AnyFlatSpec with Matchers:
  given CanEqual[Environment, Environment] = CanEqual.derived

  "YamlParser" should "parse a valid YAML configuration" in:
    val uri = getClass.getResource("/configuration.yml").toURI
    val yamlPath = Path.fromNioPath(java.nio.file.Paths.get(uri))
    val yamlContent = Files[IO].readAll(yamlPath).through(text.utf8.decode).compile.string.unsafeRunSync()
    val res = YamlParser.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(_) => succeed

  it should "fail with an empty YAML configuration" in:
    val emptyYamlContent = ""
    val res = YamlParser.parse[IO](emptyYamlContent).unsafeRunSync()
    res match
      case Left(_) => succeed
      case Right(_) => fail("Parsing should not succeed with empty content")

  it should "require the obstacle position" in:
    val invalidYamlContent = """
        |environment:
        |  entities:
        |    - obstacle:
        |        orientation: 0.0
        |        width: 1.0
        |        height: 1.0
        |""".stripMargin

    val res = YamlParser.parse[IO](invalidYamlContent).unsafeRunSync()
    res match
      case Left(errors) if errors.contains(MissingField("position")) => succeed
      case Left(errors) => fail(s"Parsing failed with unexpected errors: ${errors.mkString(", ")}")
      case Right(_) => fail("Parsing should not succeed with missing required fields")

  it should "work with optional fields" in:
    val yamlContent =
      """
        |simulation:
            duration: 60
        |""".stripMargin
    val res = YamlParser.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(config) =>
        val _ = config.simulation.duration shouldBe Some(60)
        val _ = config.simulation.seed should be(None)
        config.environment shouldBe Environment()

  it should "create the correct robot sensors" in:
    val yamlContent =
      """
        |environment:
        |  entities:
        |    - robot:
        |        position: [5.0, 5.0]
        |        speed: 1.0
        |        withProximitySensors: true
        |""".stripMargin

    val res = YamlParser.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(config) =>
        val _ = config.environment.entities.size shouldBe 1
        config.environment.entities.headOption match
          case Some(entity) =>
            entity match
              case robot: Robot =>
                val _ = robot.position shouldBe Point2D(5.0, 5.0)
                val _ = robot.actuators.size shouldBe 1
                robot.actuators.headOption match
                  case Some(actuator) =>
                    actuator match
                      case d: DifferentialWheelMotor =>
                        val _ = d.left.speed should be(1.0)
                        val _ = d.right.speed should be(1.0)
                      case _ => fail("Expected a DifferentialWheelMotor actuator")
                  case None => fail("Expected at least one actuator")
                robot.sensors should be(SimulationDefaults.DynamicEntity.Robot.stdProximitySensors)
              case _ => fail("Expected a Robot entity")
          case _ => fail("Expected a Robot entity")
    end match

  it should "create a robot with a custom actuator" in:
    val yamlContent =
      """
        |environment:
        |  entities:
        |    - robot:
        |        position: [5.0, 5.0]
        |        actuators:
        |          - differentialWheelMotor:
        |              leftSpeed: 3.0
        |              rightSpeed: 2.0
        |""".stripMargin

    val res = YamlParser.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(config) =>
        val _ = config.environment.entities.size shouldBe 1
        config.environment.entities.headOption match
          case Some(entity) =>
            entity match
              case robot: Robot =>
                val _ = robot.position shouldBe Point2D(5.0, 5.0)
                val _ = robot.actuators.size shouldBe 1
                robot.actuators.headOption match
                  case Some(actuator) =>
                    actuator match
                      case d: DifferentialWheelMotor =>
                        val _ = d.left.speed should be(3.0)
                        val _ = d.right.speed should be(2.0)
                      case _ => fail("Expected a DifferentialWheelMotor actuator")
                  case None => fail("Expected at least one actuator")
              case _ => fail("Expected a Robot entity")
          case _ => fail("Expected a Robot entity")
    end match

  it should "parse a robot with custom sensors" in:
    val yamlContent =
      """
        |environment:
        |  entities:
        |    - robot:
        |        position: [5.0, 5.0]
        |        sensors:
        |          - proximitySensor:
        |              distance: 0.5
        |              offset: 0.0
        |              range: 2.3
        |""".stripMargin

    val res = YamlParser.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(config) =>
        val _ = config.environment.entities.size shouldBe 1
        config.environment.entities.headOption match
          case Some(entity) =>
            entity match
              case robot: Robot =>
                val _ = robot.position shouldBe Point2D(5.0, 5.0)
                val _ = robot.sensors.size should be(1)
                robot.sensors.headOption match
                  case Some(sensor) =>
                    sensor match
                      case ProximitySensor(offset, distance, range) =>
                        val _ = offset.degrees should be(0.0)
                        val _ = distance should be(0.5)
                        val _ = range should be > 2.29
                        val _ =
                          range should be < 2.31 // Allowing a small margin of error due to floating point precision
                      case _ => fail("Expected a ProximitySensor")
                  case None => fail("Expected at least one sensor")
              case _ => fail("Expected a Robot entity")
          case _ => fail("Expected a Robot entity")
    end match

end YamlParserTest
