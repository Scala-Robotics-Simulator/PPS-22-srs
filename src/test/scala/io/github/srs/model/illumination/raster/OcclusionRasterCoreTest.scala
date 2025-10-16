package io.github.srs.model.illumination.raster

import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.illumination.model.ScaleFactor
import io.github.srs.model.illumination.raster.OpacityValue.{ Cleared, Occluded }
import io.github.srs.model.illumination.raster.{ OcclusionRaster, OcclusionRasterTestBase }

/**
 * Core tests for [[OcclusionRaster]] functionality
 */
final class OcclusionRasterCoreTest extends OcclusionRasterTestBase:

  "OcclusionRaster" should "handle empty environment correctly" in:
    given ScaleFactor = TestScaleFactor
    val env = createTestEnvironment()
    val staticGrid = OcclusionRaster.rasterizeStatics(env, testDims)
    val dynamicGrid = OcclusionRaster.rasterizeDynamics(env, testDims)

    val _ = countOccludedCells(staticGrid) shouldBe 0
    val _ = countOccludedCells(dynamicGrid) shouldBe 0
    val _ = staticGrid.forall(_.forall(_ == Cleared)) shouldBe true
    val _ = dynamicGrid.forall(_.forall(_ == Cleared)) shouldBe true

  it should "occluded obstacle in the static grid" in:
    val obstacle = StaticEntity.Obstacle(
      pos = Point2D(1.0, 1.0),
      orient = Orientation(0.0),
      width = 0.2,
      height = 0.2,
    )

    val robot = Robot(
      position = Point2D(3.0, 3.0),
      shape = io.github.srs.model.entity.ShapeType.Circle(0.1),
    )

    val env = createTestEnvironment(List(obstacle, robot))
    val staticGrid = OcclusionRaster.rasterizeStatics(env, testDims)
    val dynamicGrid = OcclusionRaster.rasterizeDynamics(env, testDims)

    // Check that the obstacle position is occluded in the static grid
    val (obsX, obsY) = worldToCell(1.0, 1.0)
    val _ = cellAt(staticGrid, obsX, obsY) shouldBe Some(Occluded)
    val _ = cellAt(dynamicGrid, obsX, obsY) shouldBe Some(Cleared)
    // Check that robot position is occluded in the dynamic grid
    val (robX, robY) = worldToCell(3.0, 3.0)
    val _ = cellAt(staticGrid, robX, robY) shouldBe Some(Cleared)
    val _ = cellAt(dynamicGrid, robX, robY) shouldBe Some(Occluded)
    val combinedGrid = OcclusionRaster.combine(staticGrid, dynamicGrid)
    // Combined grid should have both occluded
    val _ = cellAt(combinedGrid, obsX, obsY) shouldBe Some(Occluded)
    val _ = cellAt(combinedGrid, robX, robY) shouldBe Some(Occluded)

  it should "not treat lights as occluders" in:
    val light = StaticEntity.Light(
      pos = Point2D(2.5, 2.5),
      radius = 0.2,
      illuminationRadius = 1.0,
    )

    val env = createTestEnvironment(List(light))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Light position should remain cleared
    val (lightX, lightY) = worldToCell(2.5, 2.5)

    val _ = cellAt(grid, lightX, lightY) shouldBe Some(Cleared)
    val _ = countOccludedCells(grid) shouldBe 0

  it should "handle entities at grid boundaries correctly" in:
    // Place an obstacle at the edge but within bounds
    val edgeObstacle = StaticEntity.Obstacle(
      pos = Point2D(0.1, 0.1), // Just inside the boundary
      orient = Orientation(0.0),
      width = 0.1,
      height = 0.1,
    )

    val env = createTestEnvironment(List(edgeObstacle))
    val grid = OcclusionRaster.rasterizeStatics(env, testDims)

    // Should have at least one occluded cell
    val _ = countOccludedCells(grid) should be > 0

    // Cell at the obstacle position should be occluded
    val (x, y) = worldToCell(0.1, 0.1)
    val _ = cellAt(grid, x, y) shouldBe Some(Occluded)

end OcclusionRasterCoreTest
