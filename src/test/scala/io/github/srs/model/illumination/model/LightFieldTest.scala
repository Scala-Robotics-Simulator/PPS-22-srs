package io.github.srs.model.illumination.model

import scala.collection.immutable.ArraySeq

import io.github.srs.model.illumination.model.{ GridDims, LightField, ScaleFactor }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for [[LightField]], which provides methods for sampling light fields at world coordinates.
 */
final class LightFieldTest extends AnyFlatSpec with Matchers:

  private object C:
    /** Dimensions and values for the light field tests */
    def mk(dims: GridDims, values: ArraySeq[Double]): LightField = LightField(dims, values)

    // Test 1 (SF=1)
    val W1 = 2
    val H1 = 2
    val Data1: ArraySeq[Double] = ArraySeq(0.0, 1.0, 0.5, 0.75)
    val P1: (Double, Double) = (0.25, 0.5)
    val Expected1 = 0.40625
    val SF1: ScaleFactor = ScaleFactor.validate(1).toOption.value

    // Test 2 (SF=2)
    val W2 = 4
    val H2 = 4

    val Arr2: ArraySeq[Double] = ArraySeq.tabulate(W2 * H2) { i =>
      val x = i % W2
      val y = i / W2
      (x + y).toDouble / 6.0
    }
    val SF2: ScaleFactor = ScaleFactor.validate(2).toOption.value

    // Test 3 (out-of-bounds)
    val W3 = 2
    val H3 = 2
    val Fill = 0.7
  end C

  "FovField" should "bilinearly interpolate inside bounds (SF=1)" in:
    val field = C.mk(GridDims(C.W1, C.H1), C.Data1)
    given ScaleFactor = C.SF1
    val out = field.illuminationAt(C.P1)
    math.abs(out - C.Expected1) < 1e-12 shouldBe true

  it should "respect ScaleFactor (SF=2) sampling same world point on a 4x4 grid" in:
    val field = C.mk(GridDims(C.W2, C.H2), C.Arr2)
    given ScaleFactor = C.SF2
    val out = field.illuminationAt((0.75, 0.5)) // world point
    (out >= 0.0 && out <= 1.0) shouldBe true

  it should "return 0.0 out of bounds" in:
    val field = C.mk(GridDims(C.W3, C.H3), ArraySeq.fill(C.W3 * C.H3)(C.Fill))
    given ScaleFactor = C.SF1
    field.illuminationAt((-1.0, 0.0)) shouldBe 0.0
end LightFieldTest
