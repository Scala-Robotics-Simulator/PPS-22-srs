package io.github.srs.lighting

import io.github.srs.model.Cell
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class SquidLibFovTest extends AnyFlatSpec:

  private val GridSide = 3
  private val FovRadius = 1
  private val ExpectedArraySize = GridSide * GridSide
  private val TransparentGrid = Array.fill(GridSide, GridSide)(0.0)

  // — tests —
  "SquidLibFov" should "compute and return full-size visibility array" in:
    val vis = SquidLibFov.compute(TransparentGrid)(Cell(1, 1), FovRadius)
    vis.size.shouldBe(ExpectedArraySize)

  it should "report full visibility for the origin cell" in:
    val vis = SquidLibFov.compute(TransparentGrid)(Cell(1, 1), FovRadius)
    val originIdx = Cell(1, 1).y * GridSide + Cell(1, 1).x
    val originVis = vis.lift(originIdx).getOrElse(0.0)
    originVis shouldBe 1.0
