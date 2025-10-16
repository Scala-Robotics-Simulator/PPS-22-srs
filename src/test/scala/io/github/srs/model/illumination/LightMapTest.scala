package io.github.srs.model.illumination

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.*
import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.entity.dynamicentity.robot.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.engine.{ FovEngine, SquidLibFovEngine }
import io.github.srs.model.illumination.model.{ Cell, ScaleFactor }

/**
 * Unit tests for the [[LightMap]] class.
 */
final class LightMapTest extends AnyFlatSpec with Matchers:

  private val UsedFov: FovEngine = SquidLibFovEngine

  private object C:
    val EnvW = 6
    val EnvH = 6
    val S: ScaleFactor = ScaleFactor.validate(10).toOption.value
    val RobotPos: (Double, Double) = (2.5, 2.5)
    val RobotRadius = 0.16
    val LightPos: Point2D = Point2D(3.0, 3.0)
    val LightBulbRadius = 0.2
    val LightIlluminationRadius = 2.0
    val LightIntensity = 1.0
    val LightAttenuation = 1.0

  "LightMap" should "reduce illumination at the robot cell when includeDynamic = true" in:
    import cats.effect.unsafe.implicits.global

    val r = robot
      .at(C.RobotPos)
      .withShape(ShapeType.Circle(C.RobotRadius))
      .withOrientation(Orientation(0.0))

    val l = light
      .at(C.LightPos)
      .withRadius(C.LightBulbRadius)
      .withIlluminationRadius(C.LightIlluminationRadius)
      .withIntensity(C.LightIntensity)
      .withAttenuation(C.LightAttenuation)

    val envV = (environment withWidth C.EnvW withHeight C.EnvH containing r and l).validate.toOption.value
    val lm = LightMap.create[IO](C.S, UsedFov).unsafeRunSync()

    val withDyn = lm.computeField(envV, includeDynamic = true).unsafeRunSync()
    val noDyn = lm.computeField(envV, includeDynamic = false).unsafeRunSync()

    val (rx, ry) = Cell.toCellFloor(C.RobotPos)(using C.S)
    val idx = withDyn.dims.toIndex(rx, ry)

    val _ = withDyn.data.lift(idx).value should be <= noDyn.data.lift(idx).value
    noDyn.data.lift(idx).value should be > 0.0

  it should "handle empty environments (no lights → all zeros)" in:
    import cats.effect.unsafe.implicits.global

    val envV = (environment withWidth C.EnvW withHeight C.EnvH).validate.toOption.value
    val lm = LightMap.create[IO](C.S, UsedFov).unsafeRunSync()
    val field = lm.computeField(envV, includeDynamic = true).unsafeRunSync()

    all(field.data) shouldBe 0.0 +- 1e-12

end LightMapTest
