package io.github.srs.model.entity.dynamicentity.robot.behavior.behaviors

import cats.Monad
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.robot.behavior.BehaviorContext
import io.github.srs.utils.SimulationDefaults.DynamicEntity.{ MaxSpeed, MinSpeed }
import io.github.srs.utils.random.RNG

/**
 * Common utilities and types of behaviors.
 */
protected object BehaviorCommon:

  /**
   * Type alias for a behavioral decision. A decision is a function that takes a behavioral context and returns an
   * action and a new RNG state.
   *
   * @param F
   *   The effect type.
   */
  type Decision[F[_]] = Behavior[BehaviorContext, (Action[F], RNG)]

  /**
   * Clamps a value between 0.0 and 1.0.
   *
   * @param v
   *   The value to clamp.
   * @return
   *   The clamped value between 0.0 and 1.0.
   */
  inline def clamp01(v: Double): Double =
    if v <= 0.0 then 0.0 else if v >= 1.0 then 1.0 else v

  /**
   * Clamps a value between a lower and upper bound.
   *
   * @param v
   *   The value to clamp.
   * @param lo
   *   The lower bound.
   * @param hi
   *   The upper bound.
   * @return
   *   The clamped value between lo and hi.
   */
  inline def clamp(v: Double, lo: Double, hi: Double): Double =
    if v < lo then lo else if v > hi then hi else v

  /**
   * Converts an angle in degrees to a value between -180 and 180.
   *
   * @param deg
   *   The angle in degrees.
   * @return
   *   The converted angle between -180 and 180.
   */
  inline def toSignedDegrees(deg: Double): Double =
    if deg <= 180.0 then deg else deg - 360.0

  /**
   * Returns the absolute value of a signed angle.
   *
   * @param deg
   *   The angle in degrees.
   * @return
   *   The absolute value of the signed angle.
   */
  inline def absSigned(deg: Double): Double =
    val s = toSignedDegrees(deg); math.abs(s)

  /**
   * Normalizes an angle in degrees to a value between 0 and 360.
   *
   * @param deg
   *   The angle in degrees.
   * @return
   *   The normalized angle between 0 and 360.
   */
  inline def normalize360(deg: Double): Double =
    val d = deg % 360.0
    if d < 0.0 then d + 360.0 else d

  /**
   * Creates a movement action based on the specified wheel speeds. The speeds are clamped to the allowed minimum and
   * maximum values.
   *
   * @param l
   *   The left-wheel speed.
   * @param r
   *   The right-wheel speed.
   * @tparam F
   *   The effect type (e.g., IO, Future).
   * @return
   *   A custom movement action or a no-action if invalid.
   */
  def wheels[F[_]: Monad](l: Double, r: Double): Action[F] =
    val (cl, cr) = clampWheels(l, r)
    MovementActionFactory.customMove[F](cl, cr).getOrElse(NoAction[F]())

  /**
   * Returns a forward movement action.
   *
   * @tparam F
   *   The effect type (e.g., IO, Future).
   * @return
   *   A forward movement action.
   */
  def forward[F[_]]: Action[F] =
    MovementActionFactory.moveForward[F]

  /**
   * Creates a movement action based on the provided wheel speeds or returns a no-action if the speeds are invalid.
   *
   * @param l
   *   The speed to apply to the left wheel.
   * @param r
   *   The speed to apply to the right wheel.
   * @tparam F
   *   The effect type (e.g., IO, Future).
   */
  inline def moveOrNo[F[_]: cats.Monad](l: Double, r: Double): Action[F] =
    MovementActionFactory.customMove[F](l, r).getOrElse(NoAction[F]())

  /**
   * Clamps wheel speeds between the allowed minimum and maximum values.
   *
   * @param l
   *   The left-wheel speed.
   * @param r
   *   The right-wheel speed.
   * @return
   *   A tuple containing the clamped left and right-wheel speeds.
   */
  inline private def clampWheels(l: Double, r: Double): (Double, Double) =
    (clamp(l, MinSpeed, MaxSpeed), clamp(r, MinSpeed, MaxSpeed))

end BehaviorCommon
