package io.github.srs.model.entity

import io.github.srs.model.entity.Orientation
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class OrientationTest extends AnyFlatSpec with should.Matchers:

  "Orientation" should "create an instance with the given degrees" in:
    inside(Orientation(45.0)):
      case Right(o) => o.degrees should be(45.0)

  it should "convert degrees to radians correctly" in:
    inside(Orientation(90.0)):
      case Right(o) => o.toRadians should be(math.Pi / 2 +- 1e-6)
