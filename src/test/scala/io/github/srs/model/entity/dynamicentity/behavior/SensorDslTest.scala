package io.github.srs.model.entity.dynamicentity.behavior

import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.behavior.dsl.dsl.*
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

/**
 * * Unit tests for the Sensor DSL.
 *
 * This test suite verifies the behavior of the sensor DSL conditions.
 */
final class SensorDslTest extends AnyFlatSpec:

  private val front =
    ProximitySensor[Robot, Environment](Orientation(0), 0.1, 1.0)

  private def readings(d: Double): SensorReadings =
    Vector(SensorReading(front, d))

  "front < 0.3" should "be true when closer" in:
    val cond = front < 0.3
    cond(readings(0.2)) shouldBe true

  it should "be false when farther" in:
    val cond = front < 0.3
    cond(readings(0.8)) shouldBe false

  "front > 0.3" should "be true when farther" in:
    val cond = front > 0.3
    cond(readings(0.8)) shouldBe true
