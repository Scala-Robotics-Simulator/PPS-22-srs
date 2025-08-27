package io.github.srs.model.illumination.raster

import io.github.srs.model.entity.Entity
import io.github.srs.model.environment.ValidEnvironment.ValidEnvironment
import io.github.srs.model.illumination.model.{Grid, GridDims, ScaleFactor}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues.*
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.illumination.raster.OpacityValue.{Cleared, Occluded}

/**
 * Base trait for OcclusionRaster tests providing common test infrastructure
 */
trait OcclusionRasterTestBase extends AnyFlatSpec with Matchers:

  protected val TestScaleFactor: ScaleFactor = ScaleFactor.validate(10).toOption.value
  protected val TestEnvWidth = 5
  protected val TestEnvHeight = 5

  given ScaleFactor = TestScaleFactor
  given CanEqual[Double, OpacityValue] = CanEqual.derived

  protected val testDims: GridDims = GridDims(TestEnvWidth * TestScaleFactor, TestEnvHeight * TestScaleFactor)

  /**
   * Creates a validated test environment with the given entities
   */
  protected def createTestEnvironment(entities: Set[Entity] = Set.empty): ValidEnvironment =
    (environment withWidth TestEnvWidth withHeight TestEnvHeight containing entities).validate.toOption.value

  /**
   * Safely gets a cell value from the grid with bound checking
   */
  protected def cellAt(grid: Array[Array[Double]], x: Int, y: Int): Option[Double] =
    if x >= 0 && x < grid.length && y >= 0 && y < grid(x).length then Some(grid(x)(y))
    else None

  /**
   * Prints the contents of a grid by iterating through its dimensions.
   * Each row of the grid is printed as a comma-separated string of integer values.
   *
   * @param grid
   * The grid of double values to be dumped.
   * @param dims
   * The dimensions of the grid, specifying its width and height in terms of cells.
   * @return
   * Unit. The method produces output by printing the grid to the console.
   */
  def dump(grid: Grid[Double], dims: GridDims): Unit =
    for y <- 0 until dims.heightCells do
      val row = (0 until dims.widthCells).map(x => grid(x)(y).toInt).mkString(",")
      println(row)

  /**
   * Converts world coordinates to grid cell coordinates
   */
  protected def worldToCell(worldX: Double, worldY: Double): (Int, Int) =
    ((worldX * TestScaleFactor).toInt, (worldY * TestScaleFactor).toInt)

  /**
   * Counts occluded cells in a grid
   */
  protected def countOccludedCells(grid: Array[Array[Double]]): Int =
    grid.flatten.count(_ == Occluded)

  /**
   * Counts cleared cells in a grid
   */
  protected def countClearedCells(grid: Array[Array[Double]]): Int =
    grid.flatten.count(_ == Cleared)

  /**
   * Checks if a rectangular region is fully occluded
   */
  protected def isRegionOccluded(
      grid: Array[Array[Double]],
      centerX: Double,
      centerY: Double,
      halfWidth: Double,
      halfHeight: Double,
  ): Boolean =
    val (minX, maxX) = (
      ((centerX - halfWidth) * TestScaleFactor).toInt,
      ((centerX + halfWidth) * TestScaleFactor).toInt,
    )
    val (minY, maxY) = (
      ((centerY - halfHeight) * TestScaleFactor).toInt,
      ((centerY + halfHeight) * TestScaleFactor).toInt,
    )

    (minX to maxX).forall { x =>
      (minY to maxY).forall { y =>
        cellAt(grid, x, y).contains(Occluded)
      }
    }
  end isRegionOccluded

end OcclusionRasterTestBase
