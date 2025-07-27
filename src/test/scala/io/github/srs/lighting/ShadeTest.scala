package io.github.srs.lighting

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class ShadeTest extends AnyFlatSpec:

  private object IntensityLevels:
    val Full = 1.0
    val VeryHigh = 0.85
    val High = 0.65
    val Medium = 0.40
    val Low = 0.10
    val VeryLow = 0.01

  "Shade" should "provide characters for different light intensities" in:
    Seq(
      (IntensityLevels.Full, '█'),
      (IntensityLevels.VeryHigh, '█'),
      (IntensityLevels.High, '▓'),
      (IntensityLevels.Medium, '▒'),
      (IntensityLevels.Low, '░'),
      (IntensityLevels.VeryLow, '.'),
    ).foreach { case (intensity, char) =>
      Shade.char(intensity) shouldBe char
    }
end ShadeTest
