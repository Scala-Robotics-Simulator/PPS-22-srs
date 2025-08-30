package io.github.srs.model.entity.staticentity

import scala.language.postfixOps

import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.staticentity.StaticEntity.{ Boundary, Light, Obstacle }
import io.github.srs.model.entity.staticentity.dsl.BoundaryDsl.*
import io.github.srs.model.entity.staticentity.dsl.LightDsl.*
import io.github.srs.model.entity.staticentity.dsl.ObstacleDsl.*
import io.github.srs.model.entity.{ Orientation, Point2D }
import io.github.srs.model.validation.DomainError
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light.Self as LightSelf
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle.Self as ObstacleSelf
import org.scalatest.EitherValues.*
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

class StaticEntityTest extends AnyFlatSpec:

  val origin: (Double, Double) = Point2D(0, 0)
  val orientation: Orientation = Orientation(0)

  // Obstacle
  val width = 2
  val height = 3
  val expectedObstacle: Obstacle = Obstacle(pos = origin, orient = orientation, width = width, height = height)

  // Light
  val radius = 1.0
  val intensity = 1.0
  val attenuation = 1.0

  val expectedLight: Light = Light(
    pos = origin,
    orient = orientation,
    illuminationRadius = radius,
    intensity = intensity,
    attenuation = attenuation,
  )

  "obstacle" should "create a valid entity" in:
    val res = obstacle at origin withOrientation orientation withWidth width withHeight height
    inside(res.validate):
      case Right(entity: Obstacle) =>
        val _ = entity.position should be(expectedObstacle.position)
        val _ = entity.orientation should be(expectedObstacle.orientation)
        val _ = entity.width should be(expectedObstacle.width)
        entity.height should be(expectedObstacle.height)

  it should "fail when width is not positive" in:
    val res = obstacle at origin withOrientation orientation withWidth 0 withHeight height
    inside(res.validate.left.value) { case DomainError.NegativeOrZero(s"$ObstacleSelf width", _) => succeed }

  it should "fail when height is not positive" in:
    val res = obstacle at origin withOrientation orientation withWidth width withHeight 0
    inside(res.validate.left.value) { case DomainError.NegativeOrZero(s"$ObstacleSelf height", _) => succeed }

  "light" should "create a valid entity" in:
    val res =
      light at origin withOrientation orientation withIlluminationRadius radius withIntensity intensity withAttenuation attenuation
    inside(
      res.validate,
    ):
      case Right(entity) =>
        val _ = entity.position should be(expectedLight.position)
        val _ = entity.orientation should be(expectedLight.orientation)
        val _ = entity.radius should be(expectedLight.radius)
        val _ = entity.illuminationRadius should be(expectedLight.illuminationRadius)
        val _ = entity.intensity should be(expectedLight.intensity)
        entity.attenuation should be(expectedLight.attenuation)

  it should "fail when radius is not positive" in:
    val res =
      light at origin withOrientation orientation withRadius 0.0 withIlluminationRadius radius withIntensity intensity withAttenuation attenuation
    inside(res.validate.left.value) { case DomainError.NegativeOrZero(s"$LightSelf radius", _) => succeed }

  it should "fail when intensity is not positive" in:
    val res =
      light at origin withOrientation orientation withIlluminationRadius radius withIntensity 0.0 withAttenuation attenuation
    inside(res.validate.left.value) { case DomainError.NegativeOrZero(s"$LightSelf intensity", _) => succeed }

  it should "fail when attenuation is not positive" in:
    val res =
      light at origin withOrientation orientation withIlluminationRadius radius withIntensity intensity withAttenuation 0.0
    inside(res.validate.left.value) { case DomainError.NegativeOrZero(s"$LightSelf attenuation", _) => succeed }

  "boundary" should "create a valid entity" in:
    val expectedBoundary: Boundary = Boundary(pos = origin, orient = orientation, width = width, height = height)
    val res = boundary at origin withOrientation orientation withWidth width withHeight height
    inside(res.validate):
      case Right(entity) =>
        val _ = entity.position should be(expectedBoundary.position)
        val _ = entity.orientation should be(expectedBoundary.orientation)
        val _ = entity.width should be(expectedBoundary.width)
        entity.height should be(expectedBoundary.height)

  it should "fail when width is negative" in:
    val res = boundary at origin withOrientation orientation withWidth -1 withHeight height
    inside(res.validate.left.value) { case DomainError.Negative("width", _) => succeed }

  it should "fail when height is negative" in:
    val res = boundary at origin withOrientation orientation withWidth width withHeight -1
    inside(res.validate.left.value) { case DomainError.Negative("height", _) => succeed }

  it should "succeed when width and height are zero" in:
    val expectedBoundary: Boundary = Boundary(pos = origin, orient = orientation, width = 0, height = 0)
    val res = boundary at origin withOrientation orientation withWidth 0 withHeight 0
    inside(res.validate):
      case Right(entity) =>
        val _ = entity.position should be(expectedBoundary.position)
        val _ = entity.orientation should be(expectedBoundary.orientation)
        val _ = entity.width should be(expectedBoundary.width)
        entity.height should be(expectedBoundary.height)

end StaticEntityTest
