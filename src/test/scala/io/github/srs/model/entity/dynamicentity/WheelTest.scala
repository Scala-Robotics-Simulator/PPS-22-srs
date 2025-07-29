package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.entity.ShapeType
import io.github.srs.model.entity.dynamicentity.actuator.Wheel
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WheelTest extends AnyFlatSpec with Matchers:

  val shape: ShapeType.Circle = ShapeType.Circle(0.5)
  val wheel: Wheel = Wheel(1.0, shape)

  "Wheel" should "have a speed" in:
    wheel.speed should be(1.0)

  it should "be updated with a new speed" in:
    val updatedWheel: Wheel = wheel.updatedSpeed(2.0)
    updatedWheel.speed should be(2.0)

  it should "have a circular shape" in:
    wheel.shape match
      case ShapeType.Circle(radius) => radius should be(0.5)
