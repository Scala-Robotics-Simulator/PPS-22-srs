package io.github.srs.model.entity.dynamicentity.action

import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.customMove
import io.github.srs.model.validation.DomainError
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }
import org.scalatest.Inside.inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MovementActionTest extends AnyFlatSpec with Matchers:

  "Custom Movement Action" should "be created if within speed bounds" in:
    inside(customMove(0.5, 0.5)):
      case Right(movementAction) =>
        (movementAction.leftSpeed, movementAction.rightSpeed) shouldBe (0.5, 0.5)

  it should "be created if both equal the minimum speed" in:
    inside(customMove(MinSpeed, MinSpeed)):
      case Right(movementAction) =>
        (movementAction.leftSpeed, movementAction.rightSpeed) shouldBe (MinSpeed, MinSpeed)

  it should "be created if both equal the maximum speed" in:
    inside(customMove(MaxSpeed, MaxSpeed)):
      case Right(movementAction) =>
        (movementAction.leftSpeed, movementAction.rightSpeed) shouldBe (MaxSpeed, MaxSpeed)

  it should "fail if left speed is below minimum speed" in:
    inside(customMove(MinSpeed - 0.1, 0.5)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if right speed is below minimum speed" in:
    inside(customMove(0.5, MinSpeed - 0.1)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if left speed exceeds maximum speed" in:
    inside(customMove(MaxSpeed + 0.1, 0.5)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed

  it should "fail if right speed exceeds maximum speed" in:
    inside(customMove(0.5, MaxSpeed + 0.1)):
      case Left(DomainError.OutOfBounds(_, _, _, _)) => succeed
end MovementActionTest
