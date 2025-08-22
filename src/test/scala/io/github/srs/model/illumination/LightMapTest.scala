package io.github.srs.model.illumination

import scala.collection.immutable.ArraySeq
import java.util.concurrent.atomic.AtomicInteger

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.engine.FovEngine
import io.github.srs.model.illumination.model.ScaleFactor
import io.github.srs.model.illumination.model.Grid
import io.github.srs.model.illumination.model.Grid.*
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Tests for [[LightMap]] caching behaviour. */
final class LightMapTest extends AnyFlatSpec with Matchers:

  private val scale: ScaleFactor = ScaleFactor.validate(1).toOption.value

  private final class CountingFov extends FovEngine:
    val count = new AtomicInteger(0)
    override def compute(grid: Grid[Double])(sx: Int, sy: Int, r: Double): ArraySeq[Double] =
      count.incrementAndGet()
      ArraySeq.fill(grid.width * grid.height)(1.0)

  "LightMap.cached" should "reuse cached field when environment is unchanged" in:
    val light: StaticEntity.Light = StaticEntity.Light(illuminationRadius = 2.0, intensity = 1.0)
    val env = Environment(width = 3, height = 3, entities = Set(light))
    val fov = new CountingFov
    val lm = LightMap.cached[IO](fov, scale, maxEntries = 4).unsafeRunSync()
    val _ = lm.computeField(env, includeDynamic = false).unsafeRunSync()
    val _ = (fov.count.get() shouldBe 1)
    val _ = lm.computeField(env, includeDynamic = false).unsafeRunSync()
    val _ = (fov.count.get() shouldBe 1)

  it should "recompute field when lights change" in:
    val light: StaticEntity.Light = StaticEntity.Light(illuminationRadius = 2.0, intensity = 1.0)
    val env1 = Environment(width = 3, height = 3, entities = Set(light))
    val env2 = Environment(width = 3, height = 3, entities = Set(light.copy(intensity = 0.5)))
    val fov = new CountingFov
    val lm = LightMap.cached[IO](fov, scale, maxEntries = 4).unsafeRunSync()
    val _ = lm.computeField(env1, includeDynamic = false).unsafeRunSync()
    val _ = (fov.count.get() shouldBe 1)
    val _ = lm.computeField(env2, includeDynamic = false).unsafeRunSync()
    val _ = (fov.count.get() shouldBe 2)
end LightMapTest
