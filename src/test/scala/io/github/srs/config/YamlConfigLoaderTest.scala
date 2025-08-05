package io.github.srs.config

import cats.effect.IO
import fs2.io.file.{ Files, Path }
import io.github.srs.model.entity.Point2D
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.utils.SimulationDefaults
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class YamlConfigLoaderTest extends AnyFlatSpec with Matchers:
  given CanEqual[Sensor[?, ?], Sensor[?, ?]] = CanEqual.derived

  "YamlConfigLoader" should "load the correct configuration" in IO:
    val path = Path(getClass.getResource("/configuration.yml").getPath)
    val loader = YamlConfigLoader[IO](path)
    for result <- loader.load
    yield
      val _ = result shouldBe a[Right[?, SimulationConfig]]
      val config = result.toOption.value
      val _ = config.environment.width should be(10)
      val _ = config.environment.height should be(10)
      val _ = config.environment.entities.size should be(3)
      val _ = config.simulation.duration should be(1000)
      val _ = config.simulation.seed should be(42L)
      val _ = config.environment.entities.exists(_.position == Point2D(5, 5)) should be(true)
      val _ = config.environment.entities.exists(_.position == Point2D(2, 2)) should be(true)
      val _ = config.environment.entities.exists(e =>
        e.position == Point2D(3, 1) && (e match
          case Robot(_, _, _, _, sensors) =>
            sensors == SimulationDefaults.DynamicEntity.Robot.stdProximitySensors ++ SimulationDefaults.DynamicEntity.Robot.stdLightSensors),
      ) should be(true)
end YamlConfigLoaderTest
