package io.github.srs.model.entity.dynamicentity.behavior

import cats.*
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.action.*
import io.github.srs.model.entity.dynamicentity.action.MovementActionFactory.*
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.*
import io.github.srs.model.entity.dynamicentity.behavior.dsl.dsl.*
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment

/**
 * Catalogue of **stateless** behaviour rules.
 *
 * This object contains a collection of pure rules that define specific behaviors for dynamic entities. Each rule is
 * stateless and depends only on its input parameters.
 */
object Rules:

  /**
   * **Avoid Obstacle Rule**
   *
   * Defines a rule that makes the entity turn right when the front proximity sensor detects an obstacle closer than the
   * specified safe distance.
   *
   * @param front
   *   The front proximity sensor used to detect obstacles.
   * @param safeDist
   *   The threshold distance below which the entity considers an obstacle too close.
   * @tparam F
   *   The effect type, which must have a `Monad` instance.
   * @return
   *   A [[Rule]] that triggers a right turn action when the condition is met.
   */
  def avoidObstacle[F[_]: Monad](
      front: ProximitySensor[Robot, Environment],
      safeDist: Double,
  ): Rule[F, SensorReadings, Action[F, Robot]] =
    (front < safeDist) ==> turnRight

  /**
   * **Always Forward Rule**
   *
   * Defines a rule that makes the entity always move forward, regardless of the input. This is a dummy exploration rule
   * with no conditions.
   *
   * @tparam F
   *   The effect type, which must have an [[Applicative]] instance.
   * @return
   *   A [[Rule]] that always triggers a move forward action.
   */
  def alwaysForward[F[_]: Applicative]: Rule[F, SensorReadings, Action[F, Robot]] =
    always ==> moveForward
end Rules
