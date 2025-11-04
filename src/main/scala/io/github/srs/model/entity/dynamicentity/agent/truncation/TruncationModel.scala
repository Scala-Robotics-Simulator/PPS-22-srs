package io.github.srs.model.entity.dynamicentity.agent.truncation

import java.util.UUID

import scala.collection.mutable

import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.ModelModule.BaseState
import io.github.srs.model.entity.Entity

/**
 * Defines a truncation model for evaluating the outcomes of actions performed by agents in the simulation.
 *
 * @tparam E
 *   The type of entity for which the truncation model is applicable.
 */
trait TruncationModel[E]:
  def evaluate(prev: BaseState, current: BaseState, entity: E, action: Action[?]): Boolean

trait TruncationStateManager[E <: Entity, S]:
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
 * A truncation model that maintains internal state to evaluate action outcomes.
 *
 * @tparam E
 *   The type of entity for which the truncation model is applicable.
 * @tparam S
 *   The type of internal state maintained by the truncation model.
 */
trait StatefulTruncation[E <: Entity, S] extends TruncationModel[E]:
  protected def stateManager: TruncationStateManager[E, S]

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
    if curr.elapsedTime.toMillis == 100 then stateManager.resetAll() // This is the first state after an episode
    val state = stateManager.getOrCreateState(entity.id)
    val (reward, nextState) = compute(prev, curr, entity, action, state)
    stateManager.updateState(entity.id, nextState)
    reward

end StatefulTruncation
