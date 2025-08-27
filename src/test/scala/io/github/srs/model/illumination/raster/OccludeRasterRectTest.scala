package io.github.srs.model.illumination.raster

import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.illumination.raster.OpacityValue.{ Cleared, Occluded }

/**
 * Tests specifically for rectangular obstacles and their occlusion behavior
 */
final class OccludeRasterRectTest extends OcclusionRasterTestBase:

  "Rectangular obstacles" should "occlude cells they cover when axis-aligned" in:
    val obstacle = StaticEntity.Obstacle(
      pos = Point2D(2.5, 2.5),
      orient = Orientation(0.0),
      width = 0.3,
      height = 0.2,
    )

    val env = createTestEnvironment(Set(obstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Check that the center is occluded
    val (centerX, centerY) = worldToCell(2.5, 2.5)
    val _ = cellAt(grid, centerX, centerY) shouldBe Some(Occluded)

    // Check that the obstacle region is fully occluded
    val _ = isRegionOccluded(grid, 2.5, 2.5, 0.15, 0.1) shouldBe true

    // Check cells outside the obstacle are cleared
    val (outsideX, outsideY) = worldToCell(1.0, 1.0)
    cellAt(grid, outsideX, outsideY) shouldBe Some(Cleared)

  it should "handle rotation at 45 degrees correctly" in:
    val obstacle = StaticEntity.Obstacle(
      pos = Point2D(2.5, 2.5),
      orient = Orientation(45.0),
      width = 0.2,
      height = 0.2,
    )

    val env = createTestEnvironment(Set(obstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Center should still be occluded
    val (centerX, centerY) = worldToCell(2.5, 2.5)
    cellAt(grid, centerX, centerY) shouldBe Some(Cleared)

  it should "handle 90-degree rotations correctly" in:
    val obstacle = StaticEntity.Obstacle(
      pos = Point2D(2.5, 2.5),
      orient = Orientation(90.0),
      width = 0.3,
      height = 0.1,
    )

    val env = createTestEnvironment(Set(obstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // After 90-degree rotation, dimensions are swapped
    // Should occlude a vertical strip instead of horizontal
    val (centerX, centerY) = worldToCell(2.5, 2.5)
    val _ = cellAt(grid, centerX, centerY) shouldBe Some(Occluded)

    // Check a vertical extent (original width becomes height)
    val _ = cellAt(grid, centerX, centerY - 1) shouldBe Some(Occluded)
    val _ = cellAt(grid, centerX, centerY + 1) shouldBe Some(Occluded)

  it should "handle very small obstacles (sub-cell)" in:
    val tinyObstacle = StaticEntity.Obstacle(
      pos = Point2D(2.5, 2.5),
      orient = Orientation(0.0),
      width = 0.05,
      height = 0.05,
    )

    val env = createTestEnvironment(Set(tinyObstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Should still occlude at least the center cell
    val (centerX, centerY) = worldToCell(2.5, 2.5)
    cellAt(grid, centerX, centerY) shouldBe Some(Occluded)

  it should "handle exactly cell-sized obstacles" in:
    val cellSizedObstacle = StaticEntity.Obstacle(
      pos = Point2D(2.05, 2.05),
      orient = Orientation(0.0),
      width = 0.1,
      height = 0.1,
    )

    val env = createTestEnvironment(Set(cellSizedObstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    val (cellX, cellY) = worldToCell(2.00, 2.00)
    val _ = cellAt(grid, cellX, cellY) shouldBe Some(Occluded)

    // Adjacent cells should be cleared
    val _ = cellAt(grid, cellX + TestScaleFactor, cellY) shouldBe Some(Cleared)
    val _ = cellAt(grid, cellX - TestScaleFactor, cellY) shouldBe Some(Cleared)
    val _ = cellAt(grid, cellX, cellY + TestScaleFactor) shouldBe Some(Cleared)
    val _ = cellAt(grid, cellX, cellY - TestScaleFactor) shouldBe Some(Cleared)

  it should "handle obstacles with extreme aspect ratios" in:
    val wideObstacle = StaticEntity.Obstacle(
      pos = Point2D(2.5, 1.0),
      orient = Orientation(0.0),
      width = 2.0,
      height = 0.1,
    )

    val tallObstacle = StaticEntity.Obstacle(
      pos = Point2D(1.0, 2.5),
      orient = Orientation(0.0),
      width = 0.1,
      height = 2.0,
    )

    val env = createTestEnvironment(Set(wideObstacle, tallObstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Check a wide obstacle creates a horizontal strip
    // The obstacle spans from x=1.5 to x=3.5 (centered at 2.5 with width 2.0)
    val wideStrip = List(1.5, 2.0, 2.5, 3.0, 3.5)
    wideStrip.foreach { x =>
      val (cellX, cellY) = worldToCell(x, 1.0)
      cellAt(grid, cellX, cellY) shouldBe Some(Occluded)
    }

    // Check a tall obstacle creates a vertical strip
    // The obstacle spans from y=1.5 to y=3.5 (centered at 2.5 with height 2.0)
    val tallStrip = List(1.5, 2.0, 2.5, 3.0, 3.5)
    tallStrip.foreach { y =>
      val (cellX, cellY) = worldToCell(1.0, y)
      cellAt(grid, cellX, cellY) shouldBe Some(Occluded)
    }
end OccludeRasterRectTest
