package io.github.srs.model.entity

import io.github.srs.model.entity.*
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.OptionValues.convertOptionToValuable

class Point2DTest extends AnyFlatSpec with should.Matchers:

  "Point2D" should "wrap coordinates into a tuple correctly" in:
    inside(Point2D(1.0, 2.0)):
      case Right(p) => p should be(Point2D(1.0, 2.0).toOption.value)

  it should "compute the correct Euclidean distance" in:
    inside(Point2D(0.0, 0.0)):
      case Right(p1) =>
        inside(Point2D(3.0, 4.0)):
          case Right(p2) => p1.distanceTo(p2) should be(5.0)

  it should "be symmetric: distance from p1 to p2 equals distance from p2 to p1" in:
    inside(Point2D(0.0, 0.0)):
      case Right(p1) =>
        inside(Point2D(3.0, 4.0)):
          case Right(p2) =>
            p1.distanceTo(p2) should be(p2.distanceTo(p1))
