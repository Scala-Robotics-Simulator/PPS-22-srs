package io.github.srs.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import io.github.srs.model.ShapeType.*

class ShapeTypeTest extends AnyFlatSpec with should.Matchers:

  "Circle" should "expose its radius" in:
    val c = ShapeType.Circle(2.5)
    c match
      case ShapeType.Circle(radius) => radius should be (2.5)
        case _ => fail("Expected Circle shape")

  "Rectangle" should "expose its width and height" in:
    val r = ShapeType.Rectangle(3.0, 4.0)
    r match
      case ShapeType.Rectangle(width, height) =>
        (width, height) should be (3.0, 4.0)
      case _ => fail("Expected Rectangle shape")