package io.github.srs.model.illumination.raster

import io.github.srs.model.entity.{ Orientation, Point2D, ShapeType }
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.illumination.raster.OpacityValue.{ Cleared, Occluded }

/**
 * Tests specifically for circular entities (robots) and their occlusion behavior
 */
final class OcclusionRasterCircleTest extends OcclusionRasterTestBase:

  "Circular entities (Robots)" should "occlude cells within their radius" in:
    val robot = Robot(
      position = Point2D(2.5, 2.5),
      shape = ShapeType.Circle(0.16),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(robot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Center should be occluded
    val (centerX, centerY) = worldToCell(2.5, 2.5)
    val _ = cellAt(grid, centerX, centerY) shouldBe Some(Occluded)

    // Check adjacent cells within radius
    val _ = cellAt(grid, centerX + 1, centerY) shouldBe Some(Occluded)
    val _ = cellAt(grid, centerX - 1, centerY) shouldBe Some(Occluded)
    val _ = cellAt(grid, centerX, centerY + 1) shouldBe Some(Occluded)
    val _ = cellAt(grid, centerX, centerY - 1) shouldBe Some(Occluded)

    // Check cells outside radius are cleared
    val (farX, farY) = worldToCell(1.0, 1.0)
    cellAt(grid, farX, farY) shouldBe Some(Cleared)

  it should "handle very small robots (sub-cell radius)" in:
    val tinyRobot = Robot(
      position = Point2D(3.0, 3.0),
      shape = ShapeType.Circle(0.03),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(tinyRobot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Should not occlude any cells (too small)
    val (centerX, centerY) = worldToCell(3.0, 3.0)
    val _ = cellAt(grid, centerX, centerY) shouldBe Some(Cleared)

    // Entire grid should be cleared
    countOccludedCells(grid) shouldBe 0

  it should "handle large robots that span multiple cells" in:
    val largeRobot = Robot(
      position = Point2D(2.5, 2.5),
      shape = ShapeType.Circle(0.3),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(largeRobot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Should occlude a circular region
    val (centerX, centerY) = worldToCell(2.5, 2.5)

    // Check cells in a 3-cell radius
    for
      dx <- -3 to 3
      dy <- -3 to 3
      if math.sqrt(dx * dx + dy * dy) <= 3
    do
      cellAt(grid, centerX + dx, centerY + dy) match
        case Some(value) =>
          if math.sqrt(dx * dx + dy * dy) <= 2.5 then
            val _ = value shouldBe Occluded
        case None => // Outside grid bounds, ignore

  it should "handle robot at exact cell boundaries" in:
    val robot = Robot(
      position = Point2D(2.0, 2.0),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(robot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    val (cellX, cellY) = worldToCell(2.0, 2.0)

    // Should occlude cells around the boundary point
    val _ = cellAt(grid, cellX, cellY) shouldBe Some(Occluded)
    val _ = cellAt(grid, cellX - 1, cellY) shouldBe Some(Occluded)
    val _ = cellAt(grid, cellX, cellY - 1) shouldBe Some(Occluded)
    val _ = cellAt(grid, cellX - 1, cellY - 1) shouldBe Some(Occluded)

  it should "correctly handle multiple non-overlapping robots" in:
    val robot1 = Robot(
      position = Point2D(1.5, 1.5),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0),
    )

    val robot2 = Robot(
      position = Point2D(3.5, 3.5),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(robot1, robot2))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Both robot positions should be occluded
    val (r1X, r1Y) = worldToCell(1.5, 1.5)
    val (r2X, r2Y) = worldToCell(3.5, 3.5)

    val _ = cellAt(grid, r1X, r1Y) shouldBe Some(Occluded)
    val _ = cellAt(grid, r2X, r2Y) shouldBe Some(Occluded)

    // Area between them should be cleared
    val (midX, midY) = worldToCell(2.5, 2.5)
    cellAt(grid, midX, midY) shouldBe Some(Cleared)

  it should "handle robots at environment edges" in:
    val edgeRobot = Robot(
      position = Point2D(0.15, 2.5),
      shape = ShapeType.Circle(0.1),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(edgeRobot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Should occlude cells near the edge
    val (cellX, cellY) = worldToCell(0.15, 2.5)
    val _ = cellAt(grid, cellX, cellY) shouldBe Some(Occluded)

    // Should handle edge clipping correctly
    countOccludedCells(grid) should be > 0

  it should "distinguish between small and large robots correctly" in:
    val smallRobot = Robot(
      position = Point2D(1.5, 1.5),
      shape = ShapeType.Circle(0.05),
      orientation = Orientation(0),
    )

    val largeRobot = Robot(
      position = Point2D(3.5, 3.5),
      shape = ShapeType.Circle(0.08),
      orientation = Orientation(0),
    )

    val env = createTestEnvironment(Set(smallRobot, largeRobot))
    val grid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Small robot should not occlude
    val (smallX, smallY) = worldToCell(1.5, 1.5)
    val _ = cellAt(grid, smallX, smallY) shouldBe Some(Cleared)

    // Large robot should occlude
    val (largeX, largeY) = worldToCell(3.5, 3.5)
    val _ = cellAt(grid, largeX, largeY) shouldBe Some(Occluded)

end OcclusionRasterCircleTest
