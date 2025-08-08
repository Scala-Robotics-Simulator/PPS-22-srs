package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.ShapeType
import io.github.srs.model.entity.dynamicentity.actuator.Wheel
import org.scalatest.flatspec.AnyFlatSpec
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.*
import org.scalatest.matchers.should.Matchers
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor

class DifferentialWheelMotorDslTest extends AnyFlatSpec with Matchers:
  given CanEqual[Wheel, Wheel] = CanEqual.derived
  given CanEqual[ShapeType, ShapeType] = CanEqual.derived

  "DifferentialWheelMotor DSL" should "create a differential wheel motor with default wheels" in:
    val motor = differentialWheelMotor
    val _ = motor.left.speed shouldBe DifferentialWheelMotor.Wheel.defaultSpeed
    val _ = motor.right.speed shouldBe DifferentialWheelMotor.Wheel.defaultSpeed
    val _ = motor.left.shape shouldBe DifferentialWheelMotor.Wheel.defaultShape
    motor.right.shape shouldBe DifferentialWheelMotor.Wheel.defaultShape

  it should "set the speed of both wheels using ws" in:
    val speed = 5.0
    val motor = differentialWheelMotor.ws(speed)
    val _ = motor.left.speed shouldBe speed
    motor.right.speed shouldBe speed
