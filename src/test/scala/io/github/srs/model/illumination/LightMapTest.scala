package io.github.srs.model.illumination

import java.util.concurrent.atomic.AtomicReference

import scala.collection.immutable.ArraySeq

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Orientation, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.engine.{ FovEngine, SquidLibFovEngine }
import io.github.srs.model.illumination.model.{ GridDims, LightField, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Tests for [[LightMap]], stateless + cached. */
final class LightMapTest extends AnyFlatSpec with Matchers:

  private object C:
    val SF: ScaleFactor = ScaleFactor.validate(1).toOption.value // 1 cell == 1 m
    val EnvW = 3; val EnvH = 3
    val WallPos: (Double, Double) = (0.5, 0.5)
    val WallW = 1.0
    val WallH = 1.0
    val RobotPos: (Double, Double) = (2.5, 2.5)
    val RobotR: Double = math.sqrt(0.5)
    val RobotR2 = 0.68
    val LightPos: (Double, Double) = (1.6, 1.6)
    val LightR = 1.3
    val LightI = 1.0
    val Orient0: Orientation = Orientation(0)

  private given ScaleFactor = C.SF

  /** 3x3 environment fixture: 1x1 wall, 1 robot (no collision), 1 light. */
  private def env3x3(): Environment =
    val wall = StaticEntity.Obstacle(C.WallPos, C.Orient0, width = C.WallW, height = C.WallH)
    val bot =
      (robot at C.RobotPos withShape ShapeType.Circle(C.RobotR) withOrientation C.Orient0).validate.toOption.value
    val light = StaticEntity.Light(C.LightPos, C.Orient0, illuminationRadius = C.LightR, intensity = C.LightI)
    (environment withWidth C.EnvW withHeight C.EnvH containing wall containing bot containing light)
      .validate(insertBoundaries = false)
      .toOption
      .value

  /** FoV engine that records the last grid reference (to detect Prepared reuse). */
  private final class TappingFov(delegate: FovEngine) extends FovEngine:
    private val ref = new AtomicReference[Option[Array[Array[Double]]]](None)
    def lastGrid: Option[Array[Array[Double]]] = ref.get()

    /** Compute method that captures the grid reference used in the FOV computation */
    override def compute(grid: Array[Array[Double]])(sx: Int, sy: Int, r: Double): ArraySeq[Double] =
      ref.set(Some(grid))
      delegate.compute(grid)(sx, sy, r)

    /** Create a static LightMap instance with the given FoV engine and scale. */
  private def envWithStaticChanged(base: Environment): Environment =
    val biggerWall = StaticEntity.Obstacle(C.WallPos, C.Orient0, width = C.WallW + 0.2, height = C.WallH)
    (environment withWidth base.width withHeight base.height
      containing (base.entities.filter { case _: StaticEntity.Obstacle => false; case _ => true } + biggerWall))
      .validate(insertBoundaries = false)
      .toOption
      .value

  /** Run the light map creation and return the light field. */
  private def runOnce(env: Environment, scale: ScaleFactor): LightField =
    (for
      lm <- LightMap.create[IO](SquidLibFovEngine, scale)
      fld <- lm.computeField(env, includeDynamic = true)
    yield fld).unsafeRunSync()

  // ---------- Stateless LightMap checks --------------------------------------

  "LightMap" should "produce a field with the expected shape" in:
    val env = env3x3()
    val dims = GridDims.from(env)(summon[ScaleFactor])
    val fld = runOnce(env, summon[ScaleFactor])
    (fld.width, fld.height) shouldBe (dims.widthCells, dims.heightCells)

  it should "produce intensities within [0,1]" in:
    val env = env3x3()
    val fld = runOnce(env, summon[ScaleFactor])
    fld.data.forall(d => d >= 0.0 && d <= 1.0) shouldBe true

  it should "be idempotent for the same environment (value equality)" in:
    val env = env3x3()
    val f1 = runOnce(env, summon[ScaleFactor])
    val f2 = runOnce(env, summon[ScaleFactor])
    f1 shouldBe f2

  // ---------- Cached LightMap checks -----------------------------------------

  it should "reuse Prepared when statics are unchanged (includeDynamic=false)" in:
    val env = env3x3()
    val fov = TappingFov(SquidLibFovEngine)
    val lm = LightMap.cached[IO](fov, summon[ScaleFactor], maxEntries = 8).unsafeRunSync()

    val _ = lm.computeField(env, includeDynamic = false).unsafeRunSync()
    val g1 = fov.lastGrid.value

    val _ = lm.computeField(env, includeDynamic = false).unsafeRunSync()
    val g2 = fov.lastGrid.value

    (g1 eq g2) shouldBe true

  it should "rebuild when statics change (includeDynamic=false)" in:
    val base = env3x3()
    val envStat = envWithStaticChanged(base)

    val fov = TappingFov(SquidLibFovEngine)
    val lm = LightMap.cached[IO](fov, summon[ScaleFactor], maxEntries = 8).unsafeRunSync()

    val _ = lm.computeField(base, includeDynamic = false).unsafeRunSync()
    val g1 = fov.lastGrid.value
    val _ = lm.computeField(envStat, includeDynamic = false).unsafeRunSync()
    val g2 = fov.lastGrid.value

    (g1 eq g2) shouldBe false
end LightMapTest
