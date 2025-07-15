package io.github.srs.model

import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class Point2DTest extends AnyFlatSpec with should.Matchers:

  "Point2D" should "wrap coordinates into a tuple correctly" in:
    val p = Point2D(1.0, 2.0)
    p should be((1.0, 2.0))

  it should "compute the correct Euclidean distance" in:
    val p1 = Point2D(0.0, 0.0)
    val p2 = Point2D(3.0, 4.0)
    p1.distanceTo(p2) should be(5.0)

  it should "be symmetric: distance from p1 to p2 equals distance from p2 to p1" in:
    val p1 = Point2D(1.0, 1.0)
    val p2 = Point2D(4.0, 5.0)
    p1.distanceTo(p2) should be(p2.distanceTo(p1))
