package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.actuator.{DifferentialWheelMotor, Wheel}
import io.github.srs.model.validation.Validation
import io.github.srs.model.validation.Validation.bounded
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor.Wheel.{MaxSpeed, MinSpeed}

/**
 * DSL for creating and configuring a [[DifferentialWheelMotor]]. Provides methods to set the speed of the motor.
 */
object DifferentialWheelMotorDsl:

  /**
   * Validates a DifferentialWheelMotor to ensure it meets the domain constraints.
   *
   * @param motor
   *   the DifferentialWheelMotor to validate.
   * @return
   *   [[Right]] if the motor is valid, or [[Left]] with a validation error.
   */
  def validateDifferentialWheelMotor[E <: DynamicEntity](
      motor: DifferentialWheelMotor[E],
  ): Validation[DifferentialWheelMotor[E]] =
    for
      _ <- bounded("left speed", motor.left.speed, MinSpeed, MaxSpeed, includeMax = true)
      _ <- bounded("right speed", motor.right.speed, MinSpeed, MaxSpeed, includeMax = true)
    yield motor

  /** Creates a new instance of [[DifferentialWheelMotor]] with default wheels. */
  def differentialWheelMotor[E <: DynamicEntity]: DifferentialWheelMotor[E] =
    DifferentialWheelMotor[E](Wheel(), Wheel())


  extension[E <: DynamicEntity](motor: DifferentialWheelMotor[E])

    /**
     * Sets the speed of both wheels of the differential motor.
     *
     * @param speed
     *   the speed to set for both wheels.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated speed.
     */
    infix def ws(speed: Double): DifferentialWheelMotor[E] =
      withSpeed(speed)

    /**
     * Sets the speed of the left wheel of the differential motor.
     *
     * @param speed
     *   the speed to set for both wheels.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated speed.
     */
    infix def withSpeed(speed: Double): DifferentialWheelMotor[E] =
      motor.withLeftSpeed(speed).withRightSpeed(speed)


    /**
     * Sets the speed of the left wheel of the differential motor.
     *
     * @param speed
     *   the speed to set for the left wheel.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated left wheel speed.
     */
    infix def withLeftSpeed(speed: Double): DifferentialWheelMotor[E] =
      DifferentialWheelMotor[E](motor.left.copy(speed = speed), motor.right)

    /**
     * Sets the speed of the right wheel of the differential motor.
     *
     * @param speed
     *   the speed to set for the right wheel.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated right wheel speed.
     */
    infix def withRightSpeed(speed: Double): DifferentialWheelMotor[E] =
      DifferentialWheelMotor[E](motor.left, motor.right.copy(speed = speed))

    /**
     * Validates the differential wheel motor to ensure it meets the domain constraints.
     *
     * @return
     *   [[Right]] if the motor is valid, or [[Left]] with a validation error.
     */
    def validate: Validation[DifferentialWheelMotor[E]] =
      validateDifferentialWheelMotor(motor)
  end extension
end DifferentialWheelMotorDsl
