package io.github.srs.model.entity.staticentity.dsl

import io.github.srs.model.entity.Orientation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ObstacleDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  import ObstacleDsl.*

  "Obstacle DSL" should "create an obstacle with default properties" in:
    import io.github.srs.utils.SimulationDefaults.StaticEntity.Obstacle.*
    val entity = obstacle
    val _ = entity.pos shouldBe defaultPosition
    val _ = entity.width shouldBe defaultWidth
    val _ = entity.height shouldBe defaultHeight
    entity.orient shouldBe defaultOrientation

  it should "set the position of the obstacle" in:
    val pos = (5.0, 5.0)
    val entity = obstacle at pos
    entity.pos shouldBe pos

  it should "set the orientation of the obstacle" in:
    val orientation = Orientation(45.0)
    val entity = obstacle withOrientation orientation
    entity.orient shouldBe orientation

  it should "set the width of the obstacle" in:
    val width = 10.0
    val entity = obstacle withWidth width
    entity.width shouldBe width

  it should "set the height of the obstacle" in:
    val height = 5.0
    val entity = obstacle withHeight height
    entity.height shouldBe height

  it should "validate the obstacle with positive dimensions" in:
    val entity = obstacle withWidth 2.0 withHeight 3.0
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with zero or negative width" in:
    val entity = obstacle withWidth 0.0 withHeight 3.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "fail validation with zero or negative height" in:
    val entity = obstacle withWidth 2.0 withHeight 0.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true
end ObstacleDslTest
