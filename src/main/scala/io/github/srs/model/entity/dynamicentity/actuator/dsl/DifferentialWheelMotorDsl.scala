package io.github.srs.model.entity.dynamicentity.actuator.dsl

import io.github.srs.model.entity.dynamicentity.actuator.{ DifferentialWheelMotor, Wheel }

/**
 * DSL for creating and configuring a [[DifferentialWheelMotor]]. Provides methods to set the speed of the motor.
 */
object DifferentialWheelMotorDsl:

  /** Creates a new instance of [[DifferentialWheelMotor]] with default wheels. */
  def differentialWheelMotor: DifferentialWheelMotor =
    DifferentialWheelMotor()

  extension (motor: DifferentialWheelMotor)

    /**
     * Sets the speed of both wheels of the differential motor.
     * @param speed
     *   the speed to set for both wheels.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated speed.
     */
    infix def ws(speed: Double): DifferentialWheelMotor =
      withSpeed(speed)

    /**
     * Sets the speed of the left wheel of the differential motor.
     * @param speed
     *   the speed to set for both wheels.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated speed.
     */
    infix def withSpeed(speed: Double): DifferentialWheelMotor =
      val leftWheel = motor.left.copy(speed = speed)
      val rightWheel = motor.right.copy(speed = speed)
      DifferentialWheelMotor(leftWheel, rightWheel)

    /**
     * Sets the speed of the left wheel of the differential motor.
     * @param speed
     *   the speed to set for the left wheel.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated left wheel speed.
     */
    infix def withLeftSpeed(speed: Double): DifferentialWheelMotor =
      val leftWheel = motor.left.copy(speed = speed)
      DifferentialWheelMotor(leftWheel, motor.right)

    /**
     * Sets the speed of the right wheel of the differential motor.
     * @param speed
     *   the speed to set for the right wheel.
     * @return
     *   a new [[DifferentialWheelMotor]] instance with the updated right wheel speed.
     */
    infix def withRightSpeed(speed: Double): DifferentialWheelMotor =
      val rightWheel = motor.right.copy(speed = speed)
      DifferentialWheelMotor(motor.left, rightWheel)
  end extension
end DifferentialWheelMotorDsl
