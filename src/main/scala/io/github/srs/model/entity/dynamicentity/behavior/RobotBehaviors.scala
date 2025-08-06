package io.github.srs.model.entity.dynamicentity.behavior

import io.github.srs.model.entity.dynamicentity.DynamicEntity
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.stop
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings

/**
 * A domain‑specific alias for [[Behavior[I, A]]] where:
 *   - `I` is [[SensorReadings]], representing the data from the robot’s sensors
 *   - `A` is [[Action]], representing the actions the robot can take.
 */
type RobotBehavior[F[_], E <: DynamicEntity] = Behavior[SensorReadings, Action[F]]

/**
 * Collection of predefined behaviors for robots.
 *
 * These behaviors can be used to define common actions or states for robots in the system.
 */
object RobotBehaviors:

  /**
   * A behavior that represents the robot being idle, which means it does not perform any action.
   *
   * @return
   *   a [[RobotBehavior]] that always returns `Action.Stop`
   */
  def idle[F[_], E <: DynamicEntity]: RobotBehavior[F, E] = Behavior.pure(stop)
