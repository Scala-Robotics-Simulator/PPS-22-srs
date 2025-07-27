package io.github.srs.lighting

import scala.collection.immutable.ArraySeq

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import io.github.srs.model.Cell
import io.github.srs.model.lighting.LightState

class LightStateTest extends AnyFlatSpec:

  private val Width = 3
  private val Height = 2
  private val Data = ArraySeq(0.1, 0.2, 0.3, 0.4, 0.5, 0.6)
  private val State = LightState.fromArray(Width, Data)

  "LightState" should "expose the correct geometry" in:
    State.height.shouldBe(Height)

  it should "return the stored intensities for in‑bounds cells" in:
    Seq(
      Cell(0, 0) -> 0.1,
      Cell(1, 0) -> 0.2,
      Cell(2, 0) -> 0.3,
      Cell(0, 1) -> 0.4,
      Cell(1, 1) -> 0.5,
      Cell(2, 1) -> 0.6,
    ).foreach { (cell, expected) =>
      State.intensity(cell).shouldBe(expected)
    }

  it should "return 0.0 for out‑of‑bounds cells" in:
    Seq(Cell(-1, 0), Cell(0, -1), Cell(3, 0), Cell(0, 2), Cell(3, 2)).foreach { cell =>
      State.intensity(cell).shouldBe(0.0)
    }
end LightStateTest
