package io.github.srs.model.entity.dynamicentity.behavior.behaviors

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorContext
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.utils.random.RNG

/**
 * Common utilities and types for behaviors.
 */
object BehaviorCommon:
  type Decision[F[_]] = Behavior[BehaviorContext, (Action[F], RNG)]

  inline def clamp01(v: Double): Double =
    if v <= 0.0 then 0.0 else if v >= 1.0 then 1.0 else v

  inline def clamp(v: Double, lo: Double, hi: Double): Double =
    if v < lo then lo else if v > hi then hi else v

  inline def toSignedDegrees(deg: Double): Double =
    if deg <= 180.0 then deg else deg - 360.0

  inline def absSigned(deg: Double): Double =
    val s = toSignedDegrees(deg); math.abs(s)

  inline def normalize360(deg: Double): Double =
    val d = deg % 360.0
    if d < 0.0 then d + 360.0 else d
