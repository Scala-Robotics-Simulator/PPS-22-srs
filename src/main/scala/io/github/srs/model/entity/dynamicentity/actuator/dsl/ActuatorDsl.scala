package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
import io.github.srs.model.validation.Validation

/**
 * Provides a Domain-Specific Language (DSL) for working with actuators, including validation logic.
 */
object ActuatorDsl:

  /**
   * Validates the given actuator to ensure it meets the domain or type-specific constraints.
   *
   * @param actuator
   *   the actuator to validate, which may include various types such as DifferentialWheelMotor.
   * @return
   *   a [[Right]] containing the actuator if it is valid, or a [[Left]] with a validation error if invalid.
   */
  def validateActuator[E <: DynamicEntity](actuator: Actuator[E]): Validation[Actuator[E]] =
    actuator match
      case dwm: DifferentialWheelMotor[E] =>
        DifferentialWheelMotorDsl.validateDifferentialWheelMotor(dwm)
      case other =>
        Right(other)
