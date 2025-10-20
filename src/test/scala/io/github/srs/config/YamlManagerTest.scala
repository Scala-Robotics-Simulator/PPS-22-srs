package io.github.srs.config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.{ Files, Path }
import fs2.text
import io.github.srs.config.ConfigError.MissingField
import io.github.srs.config.yaml.YamlManager
import io.github.srs.model.Simulation
import io.github.srs.model.entity.ShapeType.Circle
import io.github.srs.model.entity.dynamicentity.actuator.DifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.SimulationDefaults
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.{ StdLightSensors, StdProximitySensors }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.utils.EqualityGivenInstances.given_CanEqual_T_T

class YamlManagerTest extends AnyFlatSpec with Matchers:
  given CanEqual[Environment, Environment] = CanEqual.derived
  given CanEqual[SimulationConfig[Environment], SimulationConfig[Environment]] = CanEqual.derived

  "YamlParser" should "parse a valid YAML configuration" in:
    val uri = getClass.getResource("/configuration.yml").toURI
    val yamlPath = Path.fromNioPath(java.nio.file.Paths.get(uri))
    val yamlContent = Files[IO].readAll(yamlPath).through(text.utf8.decode).compile.string.unsafeRunSync()
    val res = YamlManager.parse[IO](yamlContent).unsafeRunSync()
    res match
      case Left(errors) => fail(s"Parsing failed with errors: ${errors.mkString(", ")}")
      case Right(_) => succeed

  it should "fail with an empty YAML configuration" in:
    val emptyYamlContent = ""
    val res = YamlManager.parse[IO](emptyYamlContent).unsafeRunSync()
    res match
      case Left(_) => succeed
      case Right(_) => fail("Parsing should not succeed with empty content")

  it should "require the obstacle position" in:
    val invalidYamlContent = """
        |environment:
        |  entities:
        |    - obstacle:
        |        id: 00000000-0000-0000-0000-000000000000
        |        orientation: 0.0
        |        width: 1.0
        |        height: 1.0
        |""".stripMargin

    val res = YamlManager.parse[IO](invalidYamlContent).unsafeRunSync()
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
    val res = YamlManager.parse[IO](yamlContent).unsafeRunSync()
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
        |        id: 00000000-0000-0000-0000-000000000002
        |        position: [5.0, 5.0]
        |        speed: 1.0
        |        withProximitySensors: true
        |""".stripMargin

    val res = YamlManager.parse[IO](yamlContent).unsafeRunSync()
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
                      case d: DifferentialWheelMotor[Robot] =>
                        val _ = d.left.speed should be(1.0)
                        val _ = d.right.speed should be(1.0)
                      case _ => fail("Expected a DifferentialWheelMotor actuator")
                  case None => fail("Expected at least one actuator")
                robot.sensors should be(SimulationDefaults.DynamicEntity.Robot.StdProximitySensors)
              case _ => fail("Expected a Robot entity")
          case _ => fail("Expected a Robot entity")
    end match

  it should "convert a SimulationConfig to YAML" in:
    val config = SimulationConfig(
      simulation = Simulation(duration = Some(60)),
      environment = Environment(),
    )

    val expectedYaml =
      """simulation:
        |  duration: 60
        |environment:
        |  width: 10
        |  height: 10
        |""".stripMargin

    val yamlContent = YamlManager.toYaml[IO](config).unsafeRunSync()
    val loadedConfig = YamlManager.parse[IO](yamlContent).unsafeRunSync().toOption.value
    val _ = yamlContent should be(expectedYaml)
    loadedConfig shouldBe config

  it should "convert a default SimulationConfig to YAML" in:
    val config = SimulationConfig(
      simulation = Simulation(),
      environment = Environment(),
    )

    val expectedYaml =
      """environment:
        |  width: 10
        |  height: 10
        |""".stripMargin

    val yamlContent = YamlManager.toYaml[IO](config).unsafeRunSync()
    val loadedConfig = YamlManager.parse[IO](yamlContent).unsafeRunSync().toOption.value
    val _ = yamlContent should be(expectedYaml)
    loadedConfig shouldBe config

  it should "convert a SimulationConfig with custom environment to YAML" in:
    val obstacleId = SimulationDefaults.StaticEntity.Obstacle.DefaultId
    val lightId = SimulationDefaults.StaticEntity.Light.DefaultId
    val robotId = SimulationDefaults.DynamicEntity.Robot.DefaultId
    val orientation = Orientation(0.0)
    val l =
      light withId lightId at (
        1.0,
        1.0,
      ) withIntensity 0.5 withAttenuation 1.0 withIlluminationRadius 8.0 withOrientation orientation withRadius 1.0
    val o = obstacle withId obstacleId at (2.0, 2.0) withWidth 1.0 withHeight 1.0 withOrientation orientation
    val r = robot withId robotId at (4.0, 4.0) withOrientation orientation withSpeed 1.0 withShape Circle(
      0.5,
    ) withSpeed 2.0 withSensors (StdProximitySensors ++ StdLightSensors)

    val env = environment containing l and o and r

    val config = SimulationConfig(
      simulation = Simulation(seed = Some(42)),
      environment = env,
    )

    val expectedYaml =
      """simulation:
        |  seed: 42
        |environment:
        |  width: 10
        |  height: 10
        |  entities:
        |  - obstacle:
        |      id: 00000000-0000-0000-0000-000000000000
        |      position: [2.0, 2.0]
        |      orientation: 0.0
        |      width: 1.0
        |      height: 1.0
        |  - light:
        |      id: 00000000-0000-0000-0000-000000000001
        |      radius: 1.0
        |      attenuation: 1.0
        |      illuminationRadius: 8.0
        |      position: [1.0, 1.0]
        |      intensity: 0.5
        |      orientation: 0.0
        |  - robot:
        |      id: 00000000-0000-0000-0000-000000000002
        |      orientation: 0.0
        |      radius: 0.5
        |      position: [4.0, 4.0]
        |      speed: 2.0
        |      withProximitySensors: true
        |      withLightSensors: true
        |      behavior: AlwaysForward
        |""".stripMargin

    val yamlContent = YamlManager.toYaml[IO](config).unsafeRunSync()
    // Normalize the YAML content as the serialization may not preserve the order of keys
    val yamlContentSplit = yamlContent.split("\n").filter(_.nonEmpty).sorted
    val expectedYamlSplit = expectedYaml.split("\n").filter(_.nonEmpty).sorted
    val _ = for i <- yamlContentSplit.indices do yamlContentSplit(i) shouldBe expectedYamlSplit(i)
    val loadedConfig = YamlManager.parse[IO](yamlContent).unsafeRunSync()
    val loaded = loadedConfig.toOption.value

    val _ = loaded.simulation shouldBe config.simulation
    val _ = loaded.environment.width shouldBe config.environment.width
    val _ = loaded.environment.height shouldBe config.environment.height
    loaded.environment.entities should contain theSameElementsAs config.environment.entities

end YamlManagerTest
