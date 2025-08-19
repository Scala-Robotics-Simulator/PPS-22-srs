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
 * Unit tests for the `Policy` class.
 *
 * This test suite verifies the behavior of the policies defined in the `Policy` object.
 */
final class PolicyTest extends AnyFlatSpec:

  // ---------- helpers -----------------------------------------------------
  private val front =
    ProximitySensor[Robot, Environment](Orientation(0), 1.0)
  private def r(d: Double) = Vector(SensorReading(front, d))

  given CanEqual[Action[Id], Action[Id]] = CanEqual.derived

  "simple policy" should "choose avoidObstacle over forward" in:
    Policy.simple[Id](front).run(r(0.05)) shouldBe turnRight[Id]

  it should "fall back to moveForward when obstacle away" in:
    Policy.simple[Id](front).run(r(0.80)) shouldBe moveForward[Id]
