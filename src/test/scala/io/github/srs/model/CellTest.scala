package io.github.srs.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class CellTest extends AnyFlatSpec with should.Matchers:

  "Cell" should "expose x/y coordinates" in:
    val c = Cell(2, 3)
    (c.x, c.y) should be((2, 3))

  it should "be created from Point2D" in:
    val p = Point2D(2.5, 3.7)
    p.toCell should be(Cell(3, 4))
