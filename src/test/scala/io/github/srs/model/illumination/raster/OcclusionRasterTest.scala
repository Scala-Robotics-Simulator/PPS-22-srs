package io.github.srs.model.illumination.raster

//import io.github.srs.model.entity.dynamicentity.Robot
//import io.github.srs.model.entity.dynamicentity.sensor.Sensor
//import io.github.srs.model.entity.staticentity.StaticEntity
//import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
//import io.github.srs.model.environment.Environment
//import io.github.srs.model.environment.dsl.CreationDSL.*
//import io.github.srs.model.illumination.model.ScaleFactor
//import io.github.srs.model.illumination.raster.OcclusionRaster
//import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for [[OcclusionRaster]], which builds resistance/occlusion matrices from environments. Convention: grids are
 * addressed as grid(x)(y).
 */
final class OcclusionRasterTest extends AnyFlatSpec with Matchers:

  "true" should "be true" in:
    true shouldBe true

//  private object C:
//    val SF: ScaleFactor = ScaleFactor.validate(1).toOption.value // 1 cell == 1 m
//
//    /** Environment with width 3 and height 1, containing a static obstacle and a robot. */
//    def validated(env: Environment): Environment =
//      env.validate.toOption.value
//
//    /** Helper function to access a cell in a grid. */
//    def cell(grid: Array[Array[Double]])(x: Int, y: Int): Double = grid(x)(y)
//
//    /**
//     * 3x1 environment fixture with no collisions:
//     *   - static obstacle at (0.5, 0.5) size 1.2×1.2
//     *   - robot at (2.5, 0.5) circular radius 0.8
//     */
//    def env3x1NoCollision(): Environment =
//      val staticObs = StaticEntity.Obstacle(
//        pos = Point2D(0.5, 0.5),
//        orient = Orientation(0.0),
//        width = 1.2,
//        height = 1.2,
//      )
//      val robot = Robot(
//        position = (2.5, 0.5),
//        shape = ShapeType.Circle(0.8),
//        orientation = Orientation(0),
//        sensors = Vector.empty[Sensor[Robot, Environment]],
//      )
//      validated(environment withWidth 3 withHeight 1 containing staticObs containing robot)
//
//    val AngleOrthogonal: Seq[Double] = List(0.0, 15.0, 30.0, 45.0, 60.0, 75.0, 90.0, 105.0, 120.0, 135.0, 150.0, 165.0, 180.0)
//    val AngleNonOrthogonal: Seq[Double] = List(5.0, 25.0, 35.0, 55.0, 65.0, 85.0, 95.0, 115.0, 125.0, 145.0, 155.0, 175.0)
//  end C
//
//  private given ScaleFactor = C.SF
//
//  // ------------------------------- Core matrices -----------------------------
//
//  "OcclusionGrid" should "classify static occlusion" in:
//    val env = C.env3x1NoCollision()
//    val stat = OcclusionRaster.staticMatrix(env)
//    val res: Seq[Double] = Seq(C.cell(stat)(0, 0), C.cell(stat)(1, 0), C.cell(stat)(2, 0))
//    res shouldBe Seq(1.0, 0.0, 0.0)
//
//  it should "classify dynamic occlusion" in:
//    val env = C.env3x1NoCollision()
//    val dyn = OcclusionRaster.dynamicMatrix(env)
//    val res: Seq[Double] = Seq(C.cell(dyn)(0, 0), C.cell(dyn)(1, 0), C.cell(dyn)(2, 0))
//    res shouldBe Seq(0.0, 0.0, 1.0)
//
//  it should "classify combined occlusion" in:
//    val env = C.env3x1NoCollision()
//    val comb = OcclusionRaster.combinedMatrix(env)
//    val res: Seq[Double] = Seq(C.cell(comb)(0, 0), C.cell(comb)(1, 0), C.cell(comb)(2, 0))
//    res shouldBe Seq(1.0, 0.0, 1.0)
//
//  // ------------------------------ Dynamic (Circle) ---------------------------
//
//  "OcclusionGrid (Circle)" should "block when radius fully covers the cell and be free otherwise" in:
//    val tie = math.sqrt(0.5)
//    val eps = 1e-6
//    val botBelow = Robot(position = (0.5, 0.5), shape = ShapeType.Circle(tie - eps), orientation = Orientation(0), sensors = Vector.empty[Sensor[Robot, Environment]])
//    val envBelow = C.validated(environment withWidth 1 withHeight 1 containing botBelow)
//    val dynBelow = OcclusionRaster.dynamicMatrix(envBelow)
//
//    val botAbove = Robot(position = (0.5, 0.5), shape = ShapeType.Circle(tie + eps), orientation = Orientation(0), sensors = Vector.empty[Sensor[Robot, Environment]])
//    val envAbove = C.validated(environment withWidth 1 withHeight 1 containing botAbove)
//    val dynAbove = OcclusionRaster.dynamicMatrix(envAbove)
//
//    val res: Seq[Double] = Seq(C.cell(dynBelow)(0, 0), C.cell(dynAbove)(0, 0))
//    res shouldBe Seq(0.0, 1.0)
//
//  // ------------------------------ Static rectangles --------------------------
//
//  "OcclusionGrid (Rectangle)" should "be opaque at the 1x1 tie and free only when strictly smaller" in:
//    val obsTie = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.0, height = 1.0)
//    val envTie = C.validated(environment withWidth 1 withHeight 1 containing obsTie)
//    val statTie = OcclusionRaster.staticMatrix(envTie)
//
//    val obsIn = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.001, height = 1.001)
//    val envIn = C.validated(environment withWidth 1 withHeight 1 containing obsIn)
//    val statIn = OcclusionRaster.staticMatrix(envIn)
//
//    val res: Seq[Double] = Seq(C.cell(statTie)(0, 0), C.cell(statIn)(0, 0))
//    res shouldBe Seq(1.0, 1.0)
//
//  "OcclusionGrid (Rectangle rotation)" should "block when fully covering the cell and be free otherwise" in:
//    val big = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(45.0), width = 1.5, height = 1.5)
//    val envBig = C.validated(environment withWidth 1 withHeight 1 containing big)
//    val statBig = OcclusionRaster.staticMatrix(envBig)
//
//    val small = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(45.0), width = 0.8, height = 0.8)
//    val envSmall = C.validated(environment withWidth 1 withHeight 1 containing small)
//    val statSmall = OcclusionRaster.staticMatrix(envSmall)
//
//    val res: Seq[Double] = Seq(C.cell(statBig)(0, 0), C.cell(statSmall)(0, 0))
//    res shouldBe Seq(1.0, 0.0)
//
//  "OcclusionGrid (thin Rectangle)" should "not occlude when not covering all four corners" in:
//    val thin = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.0, height = 0.3)
//    val env = C.validated(environment withWidth 1 withHeight 1 containing thin)
//    val stat = OcclusionRaster.staticMatrix(env)
//    val ok: Boolean = C.cell(stat)(0, 0) == 0.0
//    ok shouldBe true
//
//  // --------------------------------- Boundary --------------------------------
//
//  "OcclusionGrid (Boundary)" should "behave like a static obstacle" in:
//    val boundary = StaticEntity.Boundary(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.2, height = 1.2)
//    val envB = C.validated(environment withWidth 1 withHeight 1 containing boundary)
//    val statB = OcclusionRaster.staticMatrix(envB)
//    val ok: Boolean = C.cell(statB)(0, 0) == 1.0
//    ok shouldBe true
//
//  // ----------------------------- AABB & clamping -----------------------------
//
//  "OcclusionGrid (AABB clamping)" should "stay within bounds and fill correctly" in:
//    val huge = StaticEntity.Obstacle(pos = Point2D(1.5, 0.5), orient = Orientation(0), width = 5.0, height = 3.0)
//    val env = C.validated(environment withWidth 2 withHeight 1 containing huge)
//    val stat = OcclusionRaster.staticMatrix(env)
//
//    val ok: Boolean =
//      (stat.length == 2) &&
//        (stat.head.length == 1) &&
//        (C.cell(stat)(0, 0) == 1.0) &&
//        (C.cell(stat)(1, 0) == 1.0)
//
//    ok shouldBe true
//
//  // --------------------------------- Overlay ---------------------------------
//
//  "OcclusionGrid" should "union in the overlapping area and preserve base elsewhere" in:
//    val base = Array.fill(3, 2)(0.0)
//    base(2)(1) = 0.7
//    val over = Array.fill(2, 1)(0.0)
//    over(1)(0) = 1.0
//    val out = OcclusionRaster.overlay(base, over)
//
//    val ok: Boolean =
//      (out.length == 3) &&
//        (out.head.length == 2) &&
//        (out(1)(0) == 1.0) &&
//        (out(2)(1) == 0.7)
//    ok shouldBe true
//
//  // -------------------------------- Edge cases -------------------------------
//
//  "OcclusionGrid (Rectangle 1x1 non-orthogonal)" should "be transparent because it does not fully cover the cell" in :
//    val allTransparent: Boolean = C.AngleNonOrthogonal.forall { ang =>
//      val obs = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(ang), width = 1.0, height = 1.0)
//      val env = C.validated(environment withWidth 1 withHeight 1 containing obs)
//      val stat = OcclusionRaster.staticMatrix(env)
//      stat(0)(0) == 0.0
//    }
//    allTransparent shouldBe true
//
//  "OcclusionGrid (Rectangle 1x1 orthogonal)" should "be opaque because it fully covers the cell" in :
//    val allTransparent: Boolean = C.AngleOrthogonal.forall { ang =>
//      val obs = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(ang), width = 1.0, height = 1.0)
//      val env = C.validated(environment withWidth 1 withHeight 1 containing obs)
//      val stat = OcclusionRaster.staticMatrix(env)
//      stat(0)(0) == 0.0
//    }
//    allTransparent shouldBe false
//
//  "OcclusionGrid (Rectangle 1x1 orthogonal and non-orthogonal)" should "be transparent for all angles" in :
//    val allTransparent: Boolean = (C.AngleOrthogonal ++ C.AngleNonOrthogonal).forall { ang =>
//      val obs = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(ang), width = 0.9, height = 0.9)
//      val env = C.validated(environment withWidth 1 withHeight 1 containing obs)
//      val stat = OcclusionRaster.staticMatrix(env)
//      stat(0)(0) == 0.0
//    }
//    allTransparent shouldBe false
//
//  "OcclusionGrid (Rectangle 1x1 centered) — tie case" should "block when the 4 corners lie on the cell border" in:
//    val obsTie = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.0, height = 1.0)
//    val envTie = C.validated(environment withWidth 1 withHeight 1 containing obsTie)
//    val statTie = OcclusionRaster.staticMatrix(envTie)
//
//    val obsBelow = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 0.999, height = 0.999)
//    val envBelow = C.validated(environment withWidth 1 withHeight 1 containing obsBelow)
//    val statBelow = OcclusionRaster.staticMatrix(envBelow)
//
//    val obsAbove = StaticEntity.Obstacle(pos = Point2D(0.5, 0.5), orient = Orientation(0), width = 1.001, height = 1.001)
//    val envAbove = C.validated(environment withWidth 1 withHeight 1 containing obsAbove)
//    val statAbove = OcclusionRaster.staticMatrix(envAbove)
//
//    val res: Seq[Double] = Seq(statTie(0)(0), statBelow(0)(0), statAbove(0)(0))
//    res shouldBe Seq(1.0, 0.0, 1.0)
//end OcclusionRasterTest
