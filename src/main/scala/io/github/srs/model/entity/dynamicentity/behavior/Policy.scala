package io.github.srs.model.entity.dynamicentity.behavior

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
  case Simple extends Policy("Simple", Behaviors.simple[IO])
  case AlwaysForward extends Policy("AlwaysForward", Behaviors.alwaysForward[IO])

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
