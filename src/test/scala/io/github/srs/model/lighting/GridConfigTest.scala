package io.github.srs.model.lighting

import io.github.srs.model.lighting.grid.{CellAggregation, GridConfig}
import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class GridConfigTest extends AnyFlatSpec with Matchers:
  

  "GridConfig.make" should "validate happy path" in :
    val res = GridConfig.make(10, CellAggregation.Max, robotsBlockLight = true, defaultCellOpacity = 0.3)
    inside(res):
      case Right(cfg) =>
        Seq(
          cfg.subdivisionsPerCell == 10,
          cfg.aggregation == CellAggregation.Max,
          cfg.robotsBlockLight,
          math.abs(cfg.defaultCellOpacity - 0.3) < 1e-12
        ).forall(identity) shouldBe true
  
  it should "reject subdivisionsPerCell < 1" in :
    val res = GridConfig.make(0)
    inside(res):
      case Left(DomainError.OutOfBounds(field, value, _, _)) =>
        (field == "subdivisionsPerCell" && value == 0.0) shouldBe true

  it should "reject defaultCellOpacity < 0" in :
    val res = GridConfig.make(1, defaultCellOpacity = -0.01)
    inside(res):
      case Left(DomainError.OutOfBounds(field, value, _, _)) =>
        (field == "defaultCellOpacity" && math.abs(value + 0.01) < 1e-12) shouldBe true

  it should "reject defaultCellOpacity > 1" in :
    val res = GridConfig.make(1, defaultCellOpacity = 1.01)
    inside(res):
      case Left(DomainError.OutOfBounds(field, value, _, _)) =>
        (field == "defaultCellOpacity" && math.abs(value - 1.01) < 1e-12) shouldBe true

  "GridConfig.validate" should "revalidate an existing instance" in :
    val cfg = GridConfig(3, CellAggregation.Mean, robotsBlockLight = false, defaultCellOpacity = 1.0)
    inside(cfg.validate):
      case Right(valid) =>
        (valid == cfg) shouldBe true