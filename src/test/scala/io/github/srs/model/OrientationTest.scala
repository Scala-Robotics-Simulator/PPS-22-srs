package io.github.srs.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class OrientationTest extends AnyFlatSpec with should.Matchers:

  "Orientation" should "create an instance with the given degrees" in:
    val o = Orientation(45.0)
    o.degrees should be(45.0)

  it should "convert degrees to radians correctly" in:
    val o = Orientation(180.0)
    o.toRadians should be(math.Pi +- 1e-6)
