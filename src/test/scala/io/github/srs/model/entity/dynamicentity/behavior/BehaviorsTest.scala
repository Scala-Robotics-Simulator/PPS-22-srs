package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import io.github.srs.model.entity.Orientation
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.action.*
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment

/**
 * Unit tests for the `Behaviors` object (merged rules + policy).
 */
final class BehaviorsTest extends AnyFlatSpec:

  // ---------- helpers -----------------------------------------------------
  private val front =
    ProximitySensor[Robot, Environment](Orientation(0), 1.0)

  private def readings(d: Double): SensorReadings =
    Vector(SensorReading(front, d))

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived

  "simple behavior" should "choose turnRight when obstacle is close" in:
    Behaviors.simple[Id].run(readings(0.05)) shouldBe turnRight[Id]

  it should "choose moveForward when obstacle is far" in:
    Behaviors.simple[Id].run(readings(0.80)) shouldBe moveForward[Id]
