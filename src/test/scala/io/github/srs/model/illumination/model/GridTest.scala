package io.github.srs.model.illumination.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.immutable.ArraySeq

/** Tests for the Grid model, which provides utility methods for creating and manipulating grids of values. */
final class GridTest extends AnyFlatSpec with Matchers:

  import Grid.*

  /** Test constants for the grid dimensions and values */
  private object C:
    val W = 3
    val H = 2
    // Overlay dimensions
    private val OverW = 2
    private val OverH = 1
    // Base grid values
    val B00 = 0.2
    val B10 = 0.3
    val B21 = 0.7
    val O10 = 0.9
    // Overlay grid values
    val FlattenW = 2
    val FlattenH = 3
    val FlattenExpected: ArraySeq[Int] = ArraySeq(0, 1, 10, 11, 20, 21)

    /** Create the base and overlay grids for testing */
    def mkBase(): Grid[Double] =
      val g = Array.fill(W, H)(0.0)
      g(0)(0) = B00
      g(1)(0) = B10
      g(2)(1) = B21
      g

    /** Create the overlay grid for testing */
    def mkOverlay(): Grid[Double] =
      val g = Array.fill(OverW, OverH)(0.0)
      g(1)(0) = O10
      g
  end C

  "Grid" should "build grid with expected size and values" in:
    val g: Grid[Int] = Grid.tabulate(C.W, C.H)((x, y) => x + 10 * y)

    val ok =
      g.width == C.W &&
        g.height == C.H &&
        g.inBounds(0, 0) && g.inBounds(2, 1) && !g.inBounds(3, 0) &&
        g(0)(0) == 0 && g(1)(0) == 1 && g(2)(1) == 12

    ok shouldBe true

  it should "take max in overlap and preserve base elsewhere" in:
    val out = Grid.overlayMax(C.mkBase(), C.mkOverlay())

    val ok =
      (out.width, out.height) == (C.W, C.H) &&
        out(1)(0) == C.O10 &&
        out(0)(0) == C.B00 &&
        out(2)(1) == C.B21

    ok shouldBe true

  it should "return base unchanged when base is empty" in:
    val nonEmpty: Grid[Double] = Array(Array(1.0), Array(2.0))
    val emptyBase: Grid[Double] = Array(Array.empty[Double])
    val out = Grid.overlayMax(emptyBase, nonEmpty)
    (out eq emptyBase) shouldBe true

  it should "return base unchanged when overlay is empty" in:
    val nonEmpty: Grid[Double] = Array(Array(1.0), Array(2.0))
    val emptyOverlay: Grid[Double] = Array.empty[Array[Double]]
    val out = Grid.overlayMax(nonEmpty, emptyOverlay)
    (out eq nonEmpty) shouldBe true

  it should "consider grids with 0 width or height as empty" in:
    val g1: Grid[Int] = Array.empty
    val g2: Grid[Int] = Array(Array.empty[Int])

    val ok = g1.isEmpty && g2.isEmpty && g2.width == 1 && g2.height == 0
    ok shouldBe true

  "Grid" should "flatten with x-fast order" in:
    val grid = Grid.tabulate(C.FlattenW, C.FlattenH)((x, y) => x + 10 * y)
    grid.flattenRowMajor shouldBe C.FlattenExpected
end GridTest
