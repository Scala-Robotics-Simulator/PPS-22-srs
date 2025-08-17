package io.github.srs.model.illumination

import scala.collection.immutable.ArraySeq

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.dsl.RobotDsl.*
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.engine.{ FovEngine, SquidLibFovEngine }
import io.github.srs.model.illumination.model.{ GridDims, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for the [[Illumination]], which provides methods for preparing static lighting data,
 */
final class IlluminationTest extends AnyFlatSpec with Matchers:

  /** Raw data structure to capture FOV computation parameters */
  private final case class Capture(
      grid: Array[Array[Double]],
      startX: Int,
      startY: Int,
      radius: Double,
  )

  /** Tapping FOV engine that captures the parameters used in the FOV computation */
  private final class TappingFov(delegate: FovEngine) extends FovEngine:
    private val ref = new java.util.concurrent.atomic.AtomicReference[Option[Capture]](None)
    def captured: Option[Capture] = ref.get()

    /** Compute method that captures the parameters used in the FOV computation */
    override def compute(grid: Array[Array[Double]])(sx: Int, sy: Int, r: Double): ArraySeq[Double] =
      ref.set(Some(Capture(grid, sx, sy, r)))
      delegate.compute(grid)(sx, sy, r)

  /**
   * Test constants and environment setup
   */
  private object C:
    val SF: ScaleFactor = ScaleFactor.validate(1).toOption.value // 1 cell == 1 m
    val EnvW = 3
    val EnvH = 3
    val WallPos: (Double, Double) = (0.5, 0.5)
    private val WallW = 1.0
    val WallH = 1.0
    val RobotPos: (Double, Double) = (2.5, 2.5)
    private val RobotR = math.sqrt(0.5)
    val NewRobotR = 0.68
    private val LightPos = (1.6, 1.6)
    private val LightR = 1.3
    private val LightI = 1.0
    val StartX = 1
    val StartY = 1
    val CeilR = 2.0
    val OnesCountWhenDyn = 2

    /**
     * 3x3 environment fixture:
     *
     *   - wall 1x1 centered at (0.5,0.5) → occludes (0,0)
     *   - robot circle r = √0.5 centered at (2.5,2.5) → occludes (2,2), no collision with wall
     *   - light at (1.6,1.6), r=1.3
     */
    def env3x3(): Environment =
      val wall = StaticEntity.Obstacle(WallPos, Orientation(0), width = WallW, height = WallH)
      val bot =
        (robot at RobotPos withShape ShapeType.Circle(RobotR) withOrientation Orientation(0)).validate.toOption.value
      val light = StaticEntity.Light(LightPos, Orientation(0), illuminationRadius = LightR, intensity = LightI)
      (environment withWidth EnvW withHeight EnvH containing wall containing bot containing light)
        .validate(insertBoundaries = false)
        .toOption
        .value
  end C

  private given ScaleFactor = C.SF

  "Lighting" should "compute dims, static matrix, and stable signature" in:
    val env = C.env3x3()
    val prepared = Illumination.prepareStatics(env, summon[ScaleFactor])
    val again = Illumination.prepareStatics(env, summon[ScaleFactor])

    val ok =
      prepared.dims == GridDims(C.EnvW, C.EnvH) &&
        prepared.staticRes.length == prepared.dims.widthCells &&
        prepared.staticRes.head.length == prepared.dims.heightCells &&
        prepared.staticSig == again.staticSig

    ok shouldBe true

  it should "reuse when only dynamic entities change" in:
    val env = C.env3x3()
    val prepared = Illumination.prepareStatics(env, summon[ScaleFactor])

    val newBot =
      (robot at C.RobotPos withShape ShapeType.Circle(C.NewRobotR) withOrientation Orientation(
        0,
      )).validate.toOption.value

    val envDynChanged =
      (environment withWidth env.width withHeight env.height
        containing (env.entities.filter {
          case _: Robot => false
          case _ => true
        } + newBot))
        .validate(insertBoundaries = false)
        .toOption
        .value

    val reused = Illumination.reuseOrRebuild(prepared, envDynChanged)
    (reused eq prepared) shouldBe true

  it should "rebuild when static entities change" in:
    val env = C.env3x3()
    val prepared = Illumination.prepareStatics(env, summon[ScaleFactor])

    val biggerWall = StaticEntity.Obstacle(C.WallPos, Orientation(0), width = 1.2, height = C.WallH)
    val envStaticChanged =
      (environment withWidth env.width withHeight env.height
        containing (env.entities.filter {
          case _: StaticEntity.Obstacle => false
          case _ => true
        } + biggerWall))
        .validate(insertBoundaries = false)
        .toOption
        .value

    val rebuilt = Illumination.reuseOrRebuild(prepared, envStaticChanged)
    (rebuilt ne prepared) shouldBe true

  it should "pass startX/startY/radius and include dynamic when requested" in:
    val env = C.env3x3()
    val prepared = Illumination.prepareStatics(env, summon[ScaleFactor])
    val fov = TappingFov(SquidLibFovEngine)

    val field = Illumination.field(env, prepared, fov, includeDynamic = true)
    val cap = fov.captured.value
    val ones = for
      x <- cap.grid.indices
      y <- cap.grid.head.indices
    yield cap.grid(x)(y)

    val ok =
      (field.width, field.height) == (prepared.dims.widthCells, prepared.dims.heightCells) &&
        cap.startX == C.StartX && cap.startY == C.StartY &&
        math.abs(cap.radius - C.CeilR) <= 1e-12 &&
        ones.count(_ == 1.0) == C.OnesCountWhenDyn &&
        cap.grid.length == C.EnvW && cap.grid.head.length == C.EnvH

    ok shouldBe true

  it should "exclude dynamic when includeDynamic=false" in:
    val env = C.env3x3()
    val prepared = Illumination.prepareStatics(env, summon[ScaleFactor])
    val fov = TappingFov(SquidLibFovEngine)

    val _ = Illumination.field(env, prepared, fov, includeDynamic = false)
    val cap = fov.captured.value

    val ok =
      cap.grid(0)(0) == 1.0 &&
        cap.grid(1)(1) == 0.0 &&
        cap.grid(2)(2) == 0.0

    ok shouldBe true
end IlluminationTest
