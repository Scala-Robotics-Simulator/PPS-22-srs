package io.github.srs.model.entity

import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Point2DTest extends AnyFlatSpec with Matchers:

  "Point2D" should "wrap coordinates into a tuple correctly" in:
    Point2D(1.0, 2.0) should be(Point2D(1.0, 2.0))

  it should "compute the correct Euclidean distance" in:
    val p1: Point2D = Point2D(0.0, 0.0)
    val p2: Point2D = Point2D(3.0, 4.0)
    p1.distanceTo(p2) should be(5.0)

  it should "be symmetric: distance from p1 to p2 equals distance from p2 to p1" in:
    val p1: Point2D = Point2D(0.0, 0.0)
    val p2: Point2D = Point2D(3.0, 4.0)
    p1.distanceTo(p2) should be(p2.distanceTo(p1))
