package io.github.srs.model.lighting.grid

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Unit tests for the [[ResistanceMap]], which represents a grid of resistance values. */
final class ResistanceMapTest extends AnyFlatSpec with Matchers:

  /** Constants for tests */
  private object C:
    val w: Int = 3
    val h: Int = 2
    val fill: Double = 0.2
    val eps: Double = 1e-12
    val half: Double = 0.5
    val inc: Double = 0.3

    val updates: Map[(Int, Int), Double] = Map(
      (1, 0) -> 0.7,
      (2, 1) -> 1.0,
    )
  import C.*

  /** Locals helpers */
  private def dimsAre(resMap: ResistanceMap, width: Int, height: Int): Boolean =
    (resMap.width == width) && (resMap.height == height)

  private def valuesAre(
      resMap: ResistanceMap,
      width: Int,
      height: Int,
      defaultValue: Double,
      overrides: Map[(Int, Int), Double],
  ): Boolean =
    (resMap.width == width && resMap.height == height) &&
      (for
        x <- 0 until width
        y <- 0 until height
      yield
        val expected = overrides.getOrElse((x, y), defaultValue)
        math.abs(resMap.valueAt(SubCell(x, y)) - expected) < eps
      ).forall(identity)

  "ResistanceMap" should "be built with a given size, applying fill then mutations" in:
    val size = SubGridSize(w, h)
    val built = ResistanceMap.build(size, fill) { grid =>
      updates.foreachEntry { case ((x, y), v) => grid(x)(y) = v }
    }
    (dimsAre(built, w, h) && valuesAre(built, w, h, fill, updates)) shouldBe true

  it should "apply a function to a single cell immutably" in:
    val size = SubGridSize(w, h)
    val m1 = ResistanceMap.filled(size, fill)
    val m2 = m1.updatedAt(SubCell(0, 1))(_ + inc)
    val before = m1.get(SubCell(0, 1)).contains(fill)
    val after = m2.get(SubCell(0, 1)).exists(v => math.abs(v - (fill + inc)) < eps)

    (before && after) shouldBe true

  it should "return a defensive copy" in:
    val size = SubGridSize(1, 1)
    val m = ResistanceMap.filled(size, 0.25)
    val arr = m.toMutableArray2D
    arr(0)(0) = 0.9

    val originalUnchanged = m.get(SubCell(0, 0)).contains(0.25)
    val copyChanged = math.abs(arr(0)(0) - 0.9) < eps

    (originalUnchanged && copyChanged) shouldBe true

  it should "report contains/get correctly at boundaries" in:
    val size = SubGridSize(w, h)
    val m = ResistanceMap.zeros(size)
    val in = SubCell(w - 1, h - 1)
    val out = List(SubCell(-1, 0), SubCell(0, -1), SubCell(w, 0), SubCell(0, h))
    val ok = m.contains(in) &&
      m.get(in).contains(0.0) &&
      out.forall(c => !m.contains(c) && m.get(c).isEmpty)
    ok shouldBe true

  it should "create constant maps via zeros/filled" in:
    val size = SubGridSize(w, h)
    val zerosM = ResistanceMap.zeros(size)
    val filledM = ResistanceMap.filled(size, fill)
    val ok = valuesAre(zerosM, w, h, 0.0, Map.empty) &&
      valuesAre(filledM, w, h, fill, Map.empty)
    ok shouldBe true
end ResistanceMapTest
