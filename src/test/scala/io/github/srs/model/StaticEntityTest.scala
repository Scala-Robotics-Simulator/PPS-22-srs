package io.github.srs.model

import io.github.srs.model.validation.DomainError
import org.scalatest.EitherValues.*
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class StaticEntityTest extends AnyFlatSpec:

  private val origin = Point2D(0, 0)
  private val orient = Orientation(0)

  "obstacle" should "return Right when width and height are positive" in:
    StaticEntity.obstacle(origin, orient, 2, 3).isRight shouldBe true

  "light" should "return Right when all parameters are positive" in:
    StaticEntity.light(origin, orient, 1.0, 1.0, 1.0).isRight shouldBe true

  it should "fail when width is not positive" in:
    val res = StaticEntity.obstacle(origin, orient, 0, 3)
    inside(res.left.value):
      case DomainError.NegativeOrZero(field, _) =>
        field shouldBe "width"

  "obstacle" should "fail when height is not positive" in:
    val res = StaticEntity.obstacle(origin, orient, 2, 0)
    inside(res.left.value):
      case DomainError.NegativeOrZero(field, _) =>
        field shouldBe "height"

  "light" should "fail when intensity is not positive" in:
    val res = StaticEntity.light(origin, orient, 1.0, 0.0, 1.0)
    inside(res.left.value):
      case DomainError.NegativeOrZero(field, _) =>
        field shouldBe "intensity"

  it should "fail when attenuation is not positive" in:
    val res = StaticEntity.light(origin, orient, 1.0, 1.0, 0.0)
    inside(res.left.value):
      case DomainError.NegativeOrZero(field, _) =>
        field shouldBe "attenuation"
