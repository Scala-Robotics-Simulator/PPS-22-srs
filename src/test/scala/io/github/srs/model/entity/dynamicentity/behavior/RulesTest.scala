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
 * Unit tests for the `Rules` behavior rules.
 *
 * This test suite verifies the behavior of specific rules
 */
final class RulesTest extends AnyFlatSpec:

  private val front =
    ProximitySensor[Robot, Environment](Orientation(0), 0.1, 1.0)

  private def readings(d: Double): SensorReadings =
    Vector(SensorReading(front, d))

  given CanEqual[Action[Id, Robot], Action[Id, Robot]] = CanEqual.derived
  given CanEqual[Option[Action[Id, Robot]], Option[Action[Id, Robot]]] = CanEqual.derived

  // ---------- tests -------------------------------------------------------
  "avoidObstacle" should "fire when closer than threshold" in:
    val rule = Rules.avoidObstacle[Id](front, safeDist = 0.30)
    rule.run(readings(0.05)) shouldBe Some(turnRight[Id, Robot])

  it should "defer otherwise" in:
    val rule = Rules.avoidObstacle[Id](front, 0.30)
    rule.run(readings(0.50)) shouldBe None

  "alwaysForward" should "always decide moveForward" in:
    val rule = Rules.alwaysForward[Id]
    rule.run(readings(0.99)) shouldBe Some(moveForward[Id, Robot])
end RulesTest
