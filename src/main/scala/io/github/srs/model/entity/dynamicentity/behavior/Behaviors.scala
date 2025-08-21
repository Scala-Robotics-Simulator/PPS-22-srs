package io.github.srs.model.entity.dynamicentity.behavior

import cats.Monad
import io.github.srs.model.entity.dynamicentity.action.{ Action, MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.{ always, Behavior, Condition }
import io.github.srs.model.entity.dynamicentity.behavior.dsl.BehaviorDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReading, SensorReadings }

/**
 * Ready-made behaviors.
 *
 * The selection step is **pure**: a [[Behavior]] is `Kleisli[Id, I, A]`. Side effects happen only when an [[Action]] is
 * executed.
 */
object Behaviors:

  /**
   * Build a condition that checks whether the "front" proximity reading (≈ 0° offset) is below a given threshold.
   *
   * @param th
   *   distance threshold (normalized reading)
   * @return
   *   a [[Condition]] over [[SensorReadings]] that holds when front < `th`
   */
  private def frontCloserThan(th: Double): Condition[SensorReadings] =
    rs =>
      rs.collectFirst {
        case SensorReading(ps: ProximitySensor[?, ?], v: Double) if math.abs(ps.offset.degrees - 0.0) <= 1e-3 => v
      }.exists(_ < th)

  /**
   * “Avoid obstacle, otherwise move forward” — pure decision returning an action.
   *
   * Semantics:
   *   - if the front proximity is `< 0.3` → turn right
   *   - else → move forward
   *   - if no rule fires → fallback to `NoAction`
   *
   * @tparam F
   *   effect type of the produced [[Action]]
   * @return
   *   a total [[Behavior]] from [[SensorReadings]] to [[Action]][F]
   */
  def simple[F[_]: Monad]: Behavior[SensorReadings, Action[F]] =
    (
      frontCloserThan(0.3) ==> MovementActionFactory.turnRight[F] |
        always ==> MovementActionFactory.moveForward[F]
    ).orElse(NoAction[F]())

  /**
   * “Always move forward” — pure decision returning an action.
   *
   * Semantics:
   *   - always move forward
   *
   * @tparam F
   *   effect type of the produced [[Action]]
   * @return
   *   a total [[Behavior]] from [[SensorReadings]] to [[Action]][F]
   */
  def alwaysForward[F[_]: Monad]: Behavior[SensorReadings, Action[F]] =
    (always ==> MovementActionFactory.moveForward[F]).orElse(NoAction[F]())
end Behaviors
