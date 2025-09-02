package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.*

/**
 * Core types for Behavior
 *
 *   - [[Behavior]] — Total, pure decision (`I => A` via `Kleisli[Id, I, A]`)
 *   - [[PartialBehavior]] — partial decision (`I => Option[A]` via `Kleisli[Option, I, A]`)
 *   - [[Condition]] — boolean predicate on the input
 *
 */
object BehaviorTypes:

  /**
   * Total, pure decision (Reader specialized to `Id`).
   *
   * @param I
   *   input type
   * @param A
   *   output type
   */
  type Behavior[I, A] = Kleisli[Id, I, A]

  /**
   * Partial decision: returns `Some(a)` if it fires, otherwise `None`.
   *
   * @param I
   *   input type
   * @param A
   *   output type
   */
  type PartialBehavior[I, A] = Kleisli[Option, I, A]

  /**
   * Boolean predicate on the input.
   *
   * @param I
   *   input type
   */
  type Condition[I] = I => Boolean

  /**
   * Constant predicate that always evaluates to `true`.
   *
   * @return
   *   a condition that ignores its input and returns `true`
   */
  val always: Condition[Any] = _ => true
end BehaviorTypes
