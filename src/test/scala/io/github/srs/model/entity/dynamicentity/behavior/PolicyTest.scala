package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import io.github.srs.model.ModelModule
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.action.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.sensor.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*

/**
 * Unit tests for the `Behaviors` object (merged rules + policy).
 */
final class BehaviorsTest extends AnyFlatSpec:

  // ---------- helpers -----------------------------------------------------
  private val front =
    ProximitySensor[Robot, ModelModule.State](Orientation(0), 1.0)

  private def readings(d: Double): SensorReadings =
    Vector(SensorReading(front, d))

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived

  "simple behavior" should "choose turnRight when obstacle is close" in:
    Policy.simple[Id].run(readings(0.05)) shouldBe turnRight[Id]

  it should "choose moveForward when obstacle is far" in:
    Policy.simple[Id].run(readings(0.80)) shouldBe moveForward[Id]
