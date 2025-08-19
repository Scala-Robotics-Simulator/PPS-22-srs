package io.github.srs.config

import java.nio.file.Files as JNIOFiles

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.io.file.{ Files, Path }
import io.github.srs.model.Simulation
import io.github.srs.model.entity.ShapeType.Circle
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.*
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.sensor.dsl.ProximitySensorDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.SimulationDefaults
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class YamlConfigManagerTest extends AnyFlatSpec with Matchers:
  given CanEqual[Sensor[?, ?], Sensor[?, ?]] = CanEqual.derived
  given CanEqual[SimulationConfig, SimulationConfig] = CanEqual.derived

  "YamlConfigLoader" should "load the correct configuration" in:
    val uri = getClass.getResource("/configuration.yml").toURI
    val path = Path.fromNioPath(java.nio.file.Paths.get(uri))
    val loader = YamlConfigManager[IO](path)
    val result = loader.load.unsafeRunSync()
    val _ = result shouldBe a[Right[?, SimulationConfig]]
    val config = result.toOption.value
    val _ = config.environment.width should be(10)
    val _ = config.environment.height should be(10)
    val _ = config.environment.entities.size should be(3)
    val _ = config.simulation.duration should be(Some(1000))
    val _ = config.simulation.seed should be(Some(42L))
    val _ = config.environment.entities.exists(_.position == Point2D(5, 5)) should be(true)
    val _ = config.environment.entities.exists(_.position == Point2D(2, 2)) should be(true)
    val _ = config.environment.entities.exists(e =>
      e.position == Point2D(3, 1) && (e match
        case Robot(_, _, _, _, _, sensors, _) =>
          sensors == SimulationDefaults.DynamicEntity.Robot.stdProximitySensors ++ SimulationDefaults.DynamicEntity.Robot.stdLightSensors),
    ) should be(true)

  "YamlConfigManager" should "save the configuration correctly" in:
    val obstacleId = SimulationDefaults.StaticEntity.Obstacle.defaultId
    val lightId = SimulationDefaults.StaticEntity.Light.defaultId
    val robotId = SimulationDefaults.DynamicEntity.Robot.defaultId
    val dwm = differentialWheelMotor withLeftSpeed 2.0 withRightSpeed 3.0
    val ps = proximitySensor withOffset Orientation(90.0) withRange 1.5
    val orientation = Orientation(0.0)
    val l =
      light withId lightId at (
        1.0,
        1.0,
      ) withIntensity 0.5 withAttenuation 1.0 withIlluminationRadius 8.0 withOrientation orientation
    val o = obstacle withId obstacleId at (2.0, 2.0) withWidth 1.0 withHeight 1.0 withOrientation orientation
    val r =
      robot withId robotId at (4.0, 4.0) withOrientation orientation withSpeed 1.0 withShape Circle(0.5) containing
        dwm and ps

    val env = environment containing l and o and r

    val config = SimulationConfig(
      simulation = Simulation(seed = Some(42)),
      environment = env,
    )
    val expectedYaml = """simulation:
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
        |      radius: 0.05
        |      attenuation: 1.0
        |      illuminationRadius: 8.0
        |      position: [1.0, 1.0]
        |      intensity: 0.5
        |      orientation: 0.0
        |  - robot:
        |      id: 00000000-0000-0000-0000-000000000002
        |      sensors:
        |      - proximitySensor:
        |          offset: 90.0
        |          range: 1.5
        |      actuators:
        |      - differentialWheelMotor:
        |          leftSpeed: 1.0
        |          rightSpeed: 1.0
        |      - differentialWheelMotor:
        |          leftSpeed: 2.0
        |          rightSpeed: 3.0
        |      orientation: 0.0
        |      radius: 0.5
        |      position: [4.0, 4.0]
        |""".stripMargin

    val path = Path.fromNioPath(JNIOFiles.createTempFile("test", ".yml"))
    val manager = YamlConfigManager[IO](path)
    manager.save(config).unsafeRunSync()
    val yamlContent = Files[IO].readAll(path).through(fs2.text.utf8.decode).compile.string.unsafeRunSync()
    // Normalize the YAML content as the serialization may not preserve the order of keys
    val yamlContentSplit = yamlContent.split("\n").filter(_.nonEmpty).sorted
    val expectedYamlSplit = expectedYaml.split("\n").filter(_.nonEmpty).sorted
    val _ = for i <- yamlContentSplit.indices do yamlContentSplit(i) shouldBe expectedYamlSplit(i)
    val loadedConfig = manager.load.unsafeRunSync().toOption.value

    loadedConfig shouldBe config

end YamlConfigManagerTest
