package io.github.srs.model.entity

import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Point2DTest extends AnyFlatSpec with Matchers:
  import Point2D.*

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

  it should "compute the sum of two points correctly" in:
    val p1: Point2D = Point2D(1.0, 2.0)
    val p2: Point2D = Point2D(3.0, 4.0)
    (p1 + p2) should be(Point2D(4.0, 6.0))

  it should "compute the difference of two points correctly" in:
    val p1: Point2D = Point2D(5.0, 7.0)
    val p2: Point2D = Point2D(3.0, 4.0)
    (p1 - p2) should be(Point2D(2.0, 3.0))

  it should "scale a point by a scalar correctly" in:
    val p: Point2D = Point2D(2.0, 3.0)
    (p * 2.0) should be(Point2D(4.0, 6.0))

  it should "compute the dot product of two points correctly" in:
    val p1: Point2D = Point2D(1.0, 2.0)
    val p2: Point2D = Point2D(3.0, 4.0)
    p1.dot(p2) should be(11.0) // 1*3 + 2*4 = 3 + 8 = 11

  it should "compute the magnitude of a point correctly" in:
    val p: Point2D = Point2D(3.0, 4.0)
    p.magnitude should be(5.0) // sqrt(3^2 + 4^2) = sqrt(9 + 16) = sqrt(25) = 5

  it should "normalize a point correctly" in:
    val p: Point2D = Point2D(3.0, 4.0)
    p.normalize should be(Point2D(0.6, 0.8)) // (3/5, 4/5)
end Point2DTest
