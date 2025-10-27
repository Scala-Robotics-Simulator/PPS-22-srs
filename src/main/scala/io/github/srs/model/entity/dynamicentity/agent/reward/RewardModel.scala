package io.github.srs.model.entity.dynamicentity.agent.reward

import java.util.UUID

import scala.collection.mutable

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.Entity
import io.github.srs.model.ModelModule.BaseState

/**
 * Defines a reward model for evaluating the outcomes of actions performed by entities within an environment.
 *
 * @tparam E
 *   The type of entity for which the reward model is applicable.
 */
trait RewardModel[E <: Entity]:
  def evaluate(prev: BaseState, current: BaseState, entity: E, action: Action[?]): Double

/**
 * Manager that maintains reward states for multiple entities.
 */
trait RewardStateManager[E <: Entity, S]:
  val states: mutable.Map[UUID, S] = mutable.Map.empty[UUID, S]
  def createState(): S
  def getOrCreateState(entityId: UUID): S = states.getOrElseUpdate(entityId, createState())

  def updateState(entityId: UUID, nextState: S): Unit =
    states.update(entityId, nextState)

  def resetState(entityId: UUID): Unit =
    states.update(entityId, createState())

  def resetAll(): Unit =
    states.clear()

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
trait StatefulReward[E <: Entity, S] extends RewardModel[E]:
  protected def stateManager: RewardStateManager[E, S]

  protected def compute(
      prev: BaseState,
      curr: BaseState,
      entity: E,
      action: Action[?],
      state: S,
  ): (Double, S)

  override final def evaluate(
      prev: BaseState,
      curr: BaseState,
      entity: E,
      action: Action[?],
  ): Double =
    if curr.elapsedTime.toMillis == 100 then stateManager.resetAll() // This is the first state after an episode
    val state = stateManager.getOrCreateState(entity.id)
    val (reward, nextState) = compute(prev, curr, entity, action, state)
    stateManager.updateState(entity.id, nextState)
    reward

end StatefulReward
