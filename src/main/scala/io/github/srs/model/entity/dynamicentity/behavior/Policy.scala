package io.github.srs.model.entity.dynamicentity.behavior

import cats.Monad
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.{ Action, NoAction }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Behavior
import io.github.srs.model.entity.dynamicentity.behavior.dsl.dsl.*
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, SensorReadings }
import io.github.srs.model.environment.Environment

/**
 * Object containing high-level policies for dynamic entities.
 */
object Policy:

  /**
   * **Simple reactive policy.**
   *
   * This policy defines a priority-based behavior for a robot:
   *
   * 1) avoid obstacles.
   *
   * 2) move forward
   *
   * 3) Perform no action [[NoAction]] as a fallback
   *
   * @param frontSensor
   *   The front proximity sensor used to detect obstacles.
   * @tparam F
   *   The effect type, which must have a `Monad` instance.
   * @return
   *   A [[Behavior]] that takes `SensorReadings` as input and produces an [[Action]].
   */
  def simple[F[_]: Monad](
      frontSensor: ProximitySensor[Robot, Environment],
  ): Behavior[F, SensorReadings, Action[F]] =

    (
      Rules.avoidObstacle(front = frontSensor, safeDist = 0.3) |
        Rules.alwaysForward[F]
    ).default(NoAction())
end Policy
