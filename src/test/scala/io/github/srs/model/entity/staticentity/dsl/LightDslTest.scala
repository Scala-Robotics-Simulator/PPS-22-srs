package io.github.srs.model.entity.staticentity.dsl

import io.github.srs.model.entity.Orientation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LightDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[Orientation, Orientation] = CanEqual.derived
  import LightDsl.*

  "Light DSL" should "create a light with default properties" in:
    import io.github.srs.utils.SimulationDefaults.StaticEntity.Light.*
    val entity = light
    val _ = entity.pos shouldBe DefaultPosition
    val _ = entity.orient shouldBe DefaultOrientation
    val _ = entity.radius shouldBe DefaultRadius
    val _ = entity.illuminationRadius shouldBe DefaultIlluminationRadius
    val _ = entity.intensity shouldBe DefaultIntensity
    entity.attenuation shouldBe DefaultAttenuation

  it should "set the position of the light" in:
    val pos = (5.0, 5.0)
    val entity = light at pos
    entity.pos shouldBe pos

  it should "set the illumination radius of the light" in:
    val radius = 10.0
    val entity = light withIlluminationRadius radius
    entity.illuminationRadius shouldBe radius

  it should "set the intensity of the light" in:
    val intensity = 5.0
    val entity = light withIntensity intensity
    entity.intensity shouldBe intensity

  it should "set the attenuation of the light" in:
    val attenuation = 2.0
    val entity = light withAttenuation attenuation
    entity.attenuation shouldBe attenuation

  it should "validate the light with positive dimensions" in:
    val entity = light withIlluminationRadius 2.0 withIntensity 3.0 withAttenuation 1.0
    val validationResult = entity.validate
    validationResult.isRight shouldBe true

  it should "fail validation with zero or negative illumination radius" in:
    val entity = light withIlluminationRadius 0.0 withIntensity 3.0 withAttenuation 1.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "fail validation with zero or negative intensity" in:
    val entity = light withIlluminationRadius 2.0 withIntensity 0.0 withAttenuation 1.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true

  it should "fail validation with zero or negative attenuation" in:
    val entity = light withIlluminationRadius 2.0 withIntensity 3.0 withAttenuation 0.0
    val validationResult = entity.validate
    validationResult.isLeft shouldBe true
end LightDslTest
