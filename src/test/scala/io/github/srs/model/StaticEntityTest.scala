package io.github.srs.model

import io.github.srs.model.validation.DomainError
import org.scalatest.EitherValues.*
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class StaticEntityTest extends AnyFlatSpec:

  given CanEqual[StaticEntity, StaticEntity] = CanEqual.derived

  val origin: (Double, Double) = Point2D(0, 0)
  val orientation: Orientation = Orientation(0)

  // Obstacle
  val width = 2
  val height = 3
  val expectedObstacle: StaticEntity = StaticEntity.Obstacle(origin, orientation, width, height)

  // Light
  val radius = 1.0
  val intensity = 1.0
  val attenuation = 1.0
  val expectedLight: StaticEntity = StaticEntity.Light(origin, orientation, radius, intensity, attenuation)

  "obstacle" should "create a valid entity" in:
    inside(StaticEntity.obstacle(origin, orientation, width, height)):
      case Right(entity) => entity shouldBe expectedObstacle

  it should "fail when width is not positive" in:
    val res = StaticEntity.obstacle(origin, orientation, 0, height)
    inside(res.left.value) { case DomainError.NegativeOrZero("width", _) => succeed }

  it should "fail when height is not positive" in:
    val res = StaticEntity.obstacle(origin, orientation, width, 0)
    inside(res.left.value) { case DomainError.NegativeOrZero("height", _) => succeed }

  "light" should "create a valid entity" in:
    inside(StaticEntity.light(origin, orientation, radius, intensity, attenuation)):
      case Right(entity) => entity shouldBe expectedLight

  it should "fail when radius is not positive" in:
    val res = StaticEntity.light(origin, orientation, 0.0, intensity, attenuation)
    inside(res.left.value) { case DomainError.NegativeOrZero("radius", _) => succeed }

  it should "fail when intensity is not positive" in:
    val res = StaticEntity.light(origin, orientation, radius, 0.0, attenuation)
    inside(res.left.value) { case DomainError.NegativeOrZero("intensity", _) => succeed }

  it should "fail when attenuation is not positive" in:
    val res = StaticEntity.light(origin, orientation, radius, intensity, 0.0)
    inside(res.left.value) { case DomainError.NegativeOrZero("attenuation", _) => succeed }

end StaticEntityTest
