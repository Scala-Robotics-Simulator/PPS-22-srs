package io.github.srs.model.entity.dynamicentity.behavior

import cats.Monad
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.behavior.behaviors.*
import io.github.srs.utils.random.RNG

/**
 * A [[Policy]] selects the next [[Action]] purely, based only on the provided [[BehaviorContext]] and returns the next
 * RNG alongside the action.
 */
enum Policy(val name: String) derives CanEqual:
  case AlwaysForward extends Policy("AlwaysForward")
  case RandomWalk extends Policy("RandomWalk")
  case ObstacleAvoidance extends Policy("ObstacleAvoidance")

  /**
   * Compute the next [[Action]] and the advanced RNG.
   *
   * @param input
   *   behavior input containing the current RNG (and other context)
   * @return
   *   (selected action, next RNG)
   */
  def run[F[_]: Monad](input: BehaviorContext): (Action[F], RNG) =
    this match
      case AlwaysForward => AlwaysForwardBehavior.decision.run(input)
      case RandomWalk => RandomWalkBehavior.decision.run(input)
      case ObstacleAvoidance => ObstacleAvoidanceBehavior.decision.run(input)

  /**
   * String representation of the policy.
   * @return
   *   name of the policy
   */
  override def toString: String = name

end Policy
