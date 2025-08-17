package io.github.srs.model.illumination

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Orientation, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.engine.SquidLibFovEngine
import io.github.srs.model.illumination.model.{ LightField, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for [[LightMap]], which provides methods for creating and managing light maps.
 */
final class LightMapTest extends AnyFlatSpec with Matchers:

  private given ScaleFactor = ScaleFactor.validate(1).toOption.value

  /**
   * 3x3 environment fixture:
   *
   *   - wall 1x1 centered at (0.5,0.5) → occludes (0,0)
   *   - robot circle r = √0.5 centered at (2.5,2 .5) → occludes (2,2), no collision with wall
   *   - light at (1.6,1.6), r=1.3
   */
  private def env3x3(): Environment =
    val wall = StaticEntity.Obstacle((0.5, 0.5), Orientation(0), width = 1.0, height = 1.0)
    val bot =
      (robot at (2.5, 2.5) withShape ShapeType.Circle(math.sqrt(0.5)) withOrientation Orientation(
        0,
      )).validate.toOption.value
    val light = StaticEntity.Light((1.6, 1.6), Orientation(0), illuminationRadius = 1.3, intensity = 1.0)

    (environment withWidth 3 withHeight 3 containing wall containing bot containing light)
      .validate(insertBoundaries = false)
      .toOption
      .value

  /** Run the light map creation and return the prepared instances and light field */
  private def runOnce(
      env: Environment,
      scale: ScaleFactor,
  ): (Illumination.Prepared, Illumination.Prepared, LightField) =
    val io =
      for
        lm <- LightMap.create[IO](SquidLibFovEngine, scale)
        p1 <- lm.prepared(env)
        p2 <- lm.prepared(env)
        fld <- lm.computeField(env, includeDynamic = true)
      yield (p1, p2, fld)
    io.unsafeRunSync()

  "LightMap" should "memoize Prepared" in:
    val env = env3x3()
    val (p1, p2, _) = runOnce(env, summon[ScaleFactor])
    (p1 eq p2) shouldBe true

  it should "produce a field with expected shape" in:
    val env = env3x3()
    val (p1, _, fld) = runOnce(env, summon[ScaleFactor])
    (fld.width, fld.height) shouldBe (p1.dims.widthCells, p1.dims.heightCells)

  it should "produce intensities within [0,1]" in:
    val env = env3x3()
    val (_, _, fld) = runOnce(env, summon[ScaleFactor])
    fld.data.forall(d => d >= 0.0 && d <= 1.0) shouldBe true

  it should "clear the cache" in:
    val env = env3x3()
    val io =
      for
        lm <- LightMap.create[IO](SquidLibFovEngine, summon[ScaleFactor])
        p1 <- lm.prepared(env)
        _ <- lm.clear
        p2 <- lm.prepared(env)
      yield (p1, p2)
    val (p1, p2) = io.unsafeRunSync()
    (p1 eq p2) shouldBe false

end LightMapTest
