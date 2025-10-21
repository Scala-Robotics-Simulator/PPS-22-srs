package io.github.srs.model.entity.dynamicentity.agent.termination

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.ModelModule.BaseState

/**
 * Defines a termination model for evaluating the outcomes of actions performed by agents in the simulation.
 *
 * @tparam E
 *   The type of entity for which the termination model is applicable.
 */
trait TerminationModel[E]:
  def evaluate(prev: BaseState, current: BaseState, entity: E, action: Action[?]): Boolean

/**
 * A termination model that maintains internal state to evaluate action outcomes.
 *
 * @param E
 *   The type of entity for which the termination model is applicable.
 * @param S
 *   The type of internal state maintained by the termination model.
 */
trait StatefulTermination[E, S] extends TerminationModel[E]:
  protected var state: S

  protected def compute(
      prev: BaseState,
      curr: BaseState,
      entity: E,
      action: Action[?],
      state: S,
  ): (Boolean, S)

  override final def evaluate(
      prev: BaseState,
      curr: BaseState,
      entity: E,
      action: Action[?],
  ): Boolean =
    val (reward, next) = compute(prev, curr, entity, action, state)
    state = next
    reward
end StatefulTermination
