package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.dsl.DifferentialWheelMotorDsl.validateDifferentialWheelMotor
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, DifferentialWheelMotor }
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
  def validateActuator(actuator: Actuator[Robot]): Validation[Actuator[Robot]] =
    actuator.validate

  extension (actuator: Actuator[Robot])

    /**
     * Validates the actuator to ensure it meets the domain constraints.
     *
     * @return
     *   [[Right]] if the actuator is valid, or [[Left]] with a validation error.
     */
    def validate: Validation[Actuator[Robot]] =
      actuator match
        case dwm: DifferentialWheelMotor => validateDifferentialWheelMotor(dwm)
end ActuatorDsl
