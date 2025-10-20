package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.{Actuator, DifferentialWheelMotor}
import io.github.srs.model.validation.Validation

/**
 * DSL for creating and configuring an [[Actuator]] for a [[Robot]]. Provides methods to validate actuators.
 */
object ActuatorDsl:

  /**
   * Validates an Actuator to ensure it meets the domain constraints.
   *
   * @param actuator
   *   the Actuator to validate.
   * @return
   *   [[Right]] if the actuator is valid, or [[Left]] with a validation error.
   */
  def validateActuator[E <: DynamicEntity](actuator: Actuator[E]): Validation[Actuator[E]] =
    actuator match
      case dwm: DifferentialWheelMotor[E] =>
        DifferentialWheelMotorDsl.validateDifferentialWheelMotor(dwm)
      case other =>
        Right(other)

end ActuatorDsl
