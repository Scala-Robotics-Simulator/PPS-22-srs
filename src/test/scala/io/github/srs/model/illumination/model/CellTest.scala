package io.github.srs.model.illumination.model

import io.github.srs.model.illumination.model.{ Cell, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for [[Cell]], which provides utility methods for converting world coordinates to cell coordinates.
 */
final class CellTest extends AnyFlatSpec with Matchers:

  private object C:
    val S10: ScaleFactor = ScaleFactor.validate(10).toOption.value
    val S4: ScaleFactor = ScaleFactor.validate(4).toOption.value
    val P1: (Double, Double) = (1.23, 4.56)
    val PNeg: (Double, Double) = (-0.01, -0.99)

  "Cell" should "floor world meters into cell coords using ScaleFactor" in:
    given ScaleFactor = C.S10
    val (x, y) = Cell.toCellFloor(C.P1)
    (x, y) shouldBe (12, 45)

  it should "handle negatives by flooring toward -inf" in:
    given ScaleFactor = C.S4
    Cell.toCellFloor(C.PNeg) shouldBe (-1, -4)

  "Cell" should "ceil radius meters to cells" in:
    given ScaleFactor = C.S4
    val res = Seq(
      Cell.radiusCells(1.00),
      Cell.radiusCells(1.01),
      Cell.radiusCells(0.0),
    )

    res shouldBe Seq(4, 5, 0)
end CellTest
