package io.github.srs.model.entity.dynamicentity

import io.github.srs.model.validation.DomainError
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActionTest extends AnyFlatSpec with Matchers:

  "Custom Movement Action" should "be created if within speed bounds" in:
    inside(Action.move(0.5, 0.5)):
      case Right(action) =>
        action.speeds shouldBe (0.5, 0.5)

  it should "be created if both equal the minimum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.minSpeed
    inside(Action.move(minSpeed, minSpeed)):
      case Right(action) =>
        action.speeds shouldBe (minSpeed, minSpeed)

  it should "be created if both equal the maximum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.maxSpeed
    inside(Action.move(maxSpeed, maxSpeed)):
      case Right(action) =>
        action.speeds shouldBe (maxSpeed, maxSpeed)

  it should "fail if left speed is below minimum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.minSpeed
    inside(Action.move(minSpeed - 0.1, 0.5)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if right speed is below minimum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.minSpeed
    inside(Action.move(0.5, minSpeed - 0.1)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if left speed exceeds maximum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.maxSpeed
    inside(Action.move(maxSpeed + 0.1, 0.5)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if right speed exceeds maximum speed" in:
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.maxSpeed
    inside(Action.move(0.5, maxSpeed + 0.1)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed
end ActionTest
