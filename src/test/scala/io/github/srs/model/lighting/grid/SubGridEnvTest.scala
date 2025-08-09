package io.github.srs.model.lighting.grid

import io.github.srs.model.entity.Point2D
import io.github.srs.model.environment.Environment
import io.github.srs.model.lighting.grid.{ SubGridEnv, SubGridIndexing }
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Unit tests for the [[SubGridEnv]] object which provides methods for converting environment coordinates to sub-cells
 * and handling sub-grid indexing.
 */
final class SubGridEnvTest extends AnyFlatSpec with Matchers:

  private object C:
    val width = 12
    val height = 7
    val subs = 4

    val pts: List[Point2D] = List(
      Point2D(-1.0, -1.0),
      Point2D(0.0, 0.0),
      Point2D(1.02, 1.98),
      Point2D(width - 0.001, height - 0.001),
      Point2D(width.toDouble, height.toDouble),
      Point2D(999.0, 999.0),
    )

  import C.*
  import io.github.srs.model.entity.Point2D.*

  private val env: Environment = Environment(width = width, height = height, entities = Set.empty)
  private val cfg: GridConfig = GridConfig.make(subdivisionsPerCell = subs).toOption.value

  "SubGridEnv" should "convert environment coordinates to sub-cells correctly" in:
    val size = SubGridIndexing.toSubGridSize(env.width, env.height, cfg.subdivisionsPerCell)
    val ok = pts.forall { p =>
      val viaPoint = SubGridEnv.toSubCellWithPoint(env, cfg)(p)
      val viaXY = SubGridEnv.toSubCell(env, cfg)(p.x, p.y)
      val viaCore = SubGridIndexing.coordinatesToSubCell(p.x, p.y, size, cfg.subdivisionsPerCell)
      (viaPoint == viaXY) && (viaXY == viaCore)
    }
    ok shouldBe true

  it should "clamp negative and overflow coordinates to valid sub-cells" in:
    val minCell = SubGridEnv.toSubCell(env, cfg)(-123.4, -7.8)
    val maxCell = SubGridEnv.toSubCell(env, cfg)(99999.0, 88888.0)
    val ok = (minCell == SubCell(0, 0)) &&
      (maxCell == SubCell(width * subs - 1, height * subs - 1))
    ok shouldBe true

  it should "round coordinates to the nearest sub-cell" in:
    val a = SubGridEnv.toSubCell(env, cfg)(0.49, 0.51)
    val b = SubGridEnv.toSubCell(env, cfg)(0.51, 0.49)
    val ok = (a == SubCell(2, 2)) && (b == SubCell(2, 2)) // subs=4 → 0.49*4≈1.96 → round=2
    ok shouldBe true
end SubGridEnvTest
