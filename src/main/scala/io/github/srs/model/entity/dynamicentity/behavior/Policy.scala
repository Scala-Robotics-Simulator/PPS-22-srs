package io.github.srs.model.entity.dynamicentity.behavior

import cats.Monad
import io.github.srs.model.entity.dynamicentity.action.{ MovementActionFactory, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.{ always, Condition }
import io.github.srs.model.entity.dynamicentity.behavior.dsl.BehaviorDsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReading }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings
import io.github.srs.model.entity.dynamicentity.action.Action
import cats.effect.IO

/**
 * Policy defines the behavior of a dynamic entity based on sensor readings.
 *
 * @param behavior
 *   The behavior to be executed based on sensor readings.
 */
enum Policy(name: String, behavior: Behavior[SensorReadings, Action[IO]]):
  case Simple extends Policy("Simple", Policy.simple[IO])
  case AlwaysForward extends Policy("AlwaysForward", Policy.alwaysForward[IO])

  /**
   * Executes the behavior defined by this policy using the provided sensor readings.
   *
   * @param readings
   *   The sensor readings to be used for executing the behavior.
   * @return
   *   An action that results from executing the behavior with the given sensor readings.
   */
  def run(readings: SensorReadings): Action[IO] =
    behavior.run(readings)

  override def toString: String = name

/**
 * Ready-made behaviors.
 *
 * The selection step is **pure**: a [[Behavior]] is `Kleisli[Id, I, A]`. Side effects happen only when an [[Action]] is
 * executed.
 */
object Policy:

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
end Policy
