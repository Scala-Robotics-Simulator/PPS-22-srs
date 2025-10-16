package io.github.srs.model.entity.dynamicentity.agent.reward

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.environment.Environment

/**
 * Defines a reward model for evaluating the outcomes of actions performed by entities within an environment.
 *
 * @tparam E
 *   The type of entity for which the reward model is applicable.
 */
trait RewardModel[E]:
  def evaluate(prev: Environment, current: Environment, entity: E, action: Action[?]): Double

/**
 * A trait for defining stateful reward models for entities interacting with an environment.
 *
 * This trait extends the [[RewardModel]] and introduces a state mechanism to maintain contextual information across
 * evaluations, allowing for more complex reward computations based on the dynamic state of the system.
 *
 * @tparam E
 *   The type of entity for which the reward model is applicable.
 * @tparam S
 *   The type of the state maintained by the reward model.
 */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
trait StatefulReward[E, S] extends RewardModel[E]:
  protected var state: S

  protected def compute(
      prev: Environment,
      curr: Environment,
      entity: E,
      action: Action[?],
      state: S,
  ): (Double, S)

  override final def evaluate(
      prev: Environment,
      curr: Environment,
      entity: E,
      action: Action[?],
  ): Double =
    val (reward, next) = compute(prev, curr, entity, action, state)
    state = next
    reward
end StatefulReward
