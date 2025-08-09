package io.github.srs.model.lighting.grid

import io.github.srs.model.lighting.grid.SubGridIndexing.{
  coordinatesToSubCell,
  subCellCenterInCoordinates,
  toSubGridSize,
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for the `SubGridMath` object, which provides mathematical operations related to sub grids.
 */
final class SubGridIndexingTest extends AnyFlatSpec with Matchers:

  private object C:
    val rawW = 10
    val rawH = 5
    val subs = 3
    val eps = 1e-9
  import C.*

  "subGridSize" should "multiply raw dimensions by subdivisions per cell" in:
    val size = toSubGridSize(rawWidth = rawW, rawHeight = rawH, subdivisionsPerCell = subs)
    (size == SubGridSize(rawW * subs, rawH * subs)) shouldBe true

  "environmentToSubCell" should "convert environment coordinates to sub-cells" in:
    val size = SubGridSize(20, 10)
    val localSubs = 4
    val maxed = coordinatesToSubCell(999.0, 999.0, size, localSubs)
    (maxed == SubCell(19, 9)) shouldBe true

  it should "round to nearest sub-cell" in:
    val size = SubGridSize(40, 40)
    val localSubs = 10
    val subCell = coordinatesToSubCell(1.02, 1.98, size, localSubs)
    (subCell == SubCell(10, 20)) shouldBe true

  "subCellCenterInCoordinates" should "return the center of a sub-cell in environment coordinates" in:
    val localSubs = 8
    val (wx, wy) = subCellCenterInCoordinates(SubCell(16, 9), localSubs)
    (math.abs(wx - (16 + 0.5) / 8.0) < eps && math.abs(wy - (9 + 0.5) / 8.0) < eps) shouldBe true
end SubGridIndexingTest
