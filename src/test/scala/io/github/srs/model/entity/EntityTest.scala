package io.github.srs.model.entity

import java.util.UUID

import io.github.srs.model.entity.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class EntityTest extends AnyFlatSpec with should.Matchers:

  "Entity trait" should "expose position, shape and orientation" in:
    val p = Point2D(0.0, 0.0)
    val o = Orientation(0.0)
    class Dummy(val id: UUID, val position: Point2D, val shape: ShapeType, val orientation: Orientation) extends Entity

    val e = Dummy(UUID.randomUUID(), position = p, shape = ShapeType.Circle(1.0), orientation = o)
    (e.position, e.shape, e.orientation) should be((p, ShapeType.Circle(1.0), o))
