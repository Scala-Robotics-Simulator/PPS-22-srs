package io.github.srs.model.entity.dynamicentity.sensor.dsl

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.sensor.dsl.ProximitySensorDsl.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Sensor.ProximitySensor
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.srs.utils.EqualityGivenInstances.given

class ProximitySensorDslTest extends AnyFlatSpec with Matchers:

  "ProximitySensor DSL" should "create a proximity sensor with default parameters" in:
    val sensor = proximitySensor
    val _ = sensor.range shouldBe ProximitySensor.DefaultRange
    sensor.offset.degrees shouldBe ProximitySensor.DefaultOffset

  it should "set the range using withRange" in:
    val range = 15.0
    val sensor = proximitySensor.withRange(range)
    sensor.range shouldBe range

  it should "set the offset using withOffset" in:
    val offset = Orientation(90.0)
    val sensor = proximitySensor.withOffset(offset)
    sensor.offset shouldBe offset

  it should "validate a valid proximity sensor" in:
    val sensor = proximitySensor
    val validation = validateProximitySensor(sensor)
    validation.isRight shouldBe true

  it should "invalidate a sensor with range below minimum" in:
    val sensor = proximitySensor.withRange(-1.0)
    val validation = validateProximitySensor(sensor)
    validation.isLeft shouldBe true
end ProximitySensorDslTest
