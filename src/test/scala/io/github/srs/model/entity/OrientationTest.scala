package io.github.srs.model.entity

import io.github.srs.model.entity.Orientation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrientationTest extends AnyFlatSpec with Matchers:

  "Orientation" should "create an instance with the given degrees" in:
    val orientation = Orientation(45.0)
    orientation.degrees should be(45.0)

  it should "convert degrees to radians correctly" in:
    val orientation = Orientation(90.0)
    orientation.toRadians should be(math.Pi / 2 +- 1e-6)
