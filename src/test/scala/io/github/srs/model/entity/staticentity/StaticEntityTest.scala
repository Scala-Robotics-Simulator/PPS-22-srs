package io.github.srs.model.entity.staticentity

import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Orientation, Point2D }
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

  val expectedLight: StaticEntity = StaticEntity.Light(
    pos = origin,
    orient = orientation,
    illuminationRadius = radius,
    intensity = intensity,
    attenuation = attenuation,
  )

  "obstacle" should "create a valid entity" in:
    val res = obstacle at origin withOrientation orientation withWidth width withHeight height
    inside(res.validate):
      case Right(entity) => entity shouldBe expectedObstacle

  it should "fail when width is not positive" in:
    val res = obstacle at origin withOrientation orientation withWidth 0 withHeight height
    inside(res.validate.left.value) { case DomainError.NegativeOrZero("width", _) => succeed }

  it should "fail when height is not positive" in:
    val res = obstacle at origin withOrientation orientation withWidth width withHeight 0
    inside(res.validate.left.value) { case DomainError.NegativeOrZero("height", _) => succeed }

  "light" should "create a valid entity" in:
    inside(
      StaticEntity.light(
        pos = origin,
        orient = orientation,
        illuminationRadius = radius,
        intensity = intensity,
        attenuation = attenuation,
      ),
    ):
      case Right(entity) => entity shouldBe expectedLight

  it should "fail when radius is not positive" in:
    val res = StaticEntity.light(
      pos = origin,
      orient = orientation,
      radius = 0.0,
      illuminationRadius = radius,
      intensity = intensity,
      attenuation = attenuation,
    )
    inside(res.left.value) { case DomainError.NegativeOrZero("radius", _) => succeed }

  it should "fail when intensity is not positive" in:
    val res = StaticEntity.light(
      pos = origin,
      orient = orientation,
      illuminationRadius = radius,
      intensity = 0.0,
      attenuation = attenuation,
    )
    inside(res.left.value) { case DomainError.NegativeOrZero("intensity", _) => succeed }

  it should "fail when attenuation is not positive" in:
    val res = StaticEntity.light(
      pos = origin,
      orient = orientation,
      illuminationRadius = radius,
      intensity = intensity,
      attenuation = 0.0,
    )
    inside(res.left.value) { case DomainError.NegativeOrZero("attenuation", _) => succeed }

  "boundary" should "create a valid entity" in:
    val expectedBoundary: StaticEntity = StaticEntity.Boundary(origin, orientation, width, height)
    inside(StaticEntity.boundary(origin, orientation, width, height)):
      case Right(entity) => entity shouldBe expectedBoundary

  it should "fail when width is negative" in:
    val res = StaticEntity.boundary(origin, orientation, -1, height)
    inside(res.left.value) { case DomainError.Negative("width", _) => succeed }

  it should "fail when height is negative" in:
    val res = StaticEntity.boundary(origin, orientation, width, -1)
    inside(res.left.value) { case DomainError.Negative("height", _) => succeed }

  it should "succeed when width and height are zero" in:
    inside(StaticEntity.boundary(origin, orientation, 0, 0)):
      case Right(entity) => entity shouldBe StaticEntity.Boundary(origin, orientation, 0, 0)

end StaticEntityTest
