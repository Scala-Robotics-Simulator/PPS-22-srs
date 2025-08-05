package io.github.srs.model.entity.staticentity.dsl

import io.github.srs.model.entity.Orientation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoundaryDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  import BoundaryDsl.*

  "Boundary DSL" should "create a boundary with default properties" in:
    import io.github.srs.utils.SimulationDefaults.StaticEntity.Boundary.*
    val entity = boundary
    val _ = entity.pos shouldBe defaultPosition
    val _ = entity.width shouldBe defaultWidth
    val _ = entity.height shouldBe defaultHeight
    entity.orient shouldBe defaultOrientation

  it should "set the position of the boundary" in:
    val pos = (5.0, 5.0)
    val entity = boundary at pos
    entity.pos shouldBe pos

  it should "set the orientation of the boundary" in:
    val orientation = Orientation(45.0)
    val entity = boundary withOrientation orientation
    entity.orient shouldBe orientation

  it should "set the width of the boundary" in:
    val width = 10.0
    val entity = boundary withWidth width
    entity.width shouldBe width

  it should "set the height of the boundary" in:
    val height = 5.0
    val entity = boundary withHeight height
    entity.height shouldBe height

  it should "validate the boundary with positive dimensions" in:
    val entity = boundary withWidth 2.0 withHeight 3.0
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "validate the boundary with zero width" in:
    val entity = boundary withWidth 0.0 withHeight 3.0
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with negative width" in:
    val entity = boundary withWidth -1.0 withHeight 3.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "validate the boundary with zero height" in:
    val entity = boundary withWidth 2.0 withHeight 0.0
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with negative height" in:
    val entity = boundary withWidth 2.0 withHeight -1.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true
end BoundaryDslTest
