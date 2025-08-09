package io.github.srs.model.lighting.grid

import io.github.srs.model.lighting.grid.{CellAggregation, GridConfig}
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for the [[GridConfig]] class.
 */
final class GridConfigTest extends AnyFlatSpec with Matchers:

  private object C:
    val subsOk = 10
    val subsBad = 0
    val opOk = 0.3
    val opLowBad = -0.01
    val opHiBad = 1.01
    val eps = 1e-12

  import C._

  "GridConfig" should "create a valid instance" in:
    val res = GridConfig.make(
      subdivisionsPerCell = subsOk,
      aggregation = CellAggregation.Max,
      robotsBlockLight = true,
      defaultCellOpacity = opOk
    )
    inside(res):
      case Right(cfg) =>
        Seq(
          cfg.subdivisionsPerCell == subsOk,
          cfg.aggregation == CellAggregation.Max,
          cfg.robotsBlockLight,
          math.abs(cfg.defaultCellOpacity - opOk) < eps
        ).forall(identity) shouldBe true

  it should "reject subdivisionsPerCell < 1" in:
    val res = GridConfig.make(subsBad)
    inside(res):
      case Left(DomainError.OutOfBounds(field, value, _, _)) =>
        (field == "subdivisionsPerCell" && value == subsBad.toDouble) shouldBe true

  it should "reject defaultCellOpacity outside [0,1]" in:
    val below = GridConfig.make(1, defaultCellOpacity = opLowBad).isLeft
    val above = GridConfig.make(1, defaultCellOpacity = opHiBad).isLeft
    (below && above) shouldBe true

end GridConfigTest
