package io.github.srs.model.entity.dynamicentity.behavior

import cats.Id
import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.*

/**
 * Types used by the Behavior DSL and policy.
 *
 * All aliases are kept package-internal so they do **not** be part of the public namespace
 */
private[behavior] object BehaviorTypes:

  /**
   * **Behavior** – a pure function in any effect [[F]].
   *
   * @param F
   *   effect type (e.g. [[Id]], [[IO]])
   * @param I
   *   input.
   * @param A
   *   output.
   */
  type Behavior[F[_], I, A] = Kleisli[F, I, A]

  /**
   * Shorthand for a pure rule (no effect).
   *
   * A rule that operates without any effect, using the [[Id]] type.
   *
   * @param I
   *   The input type.
   * @param A
   *   The output type.
   */
  type BehaviorId[I, A] = Behavior[Id, I, A]

  /**
   * **Rule** – “may decide”.
   *
   * Represents a rule that takes an input [[I]] and may produce an output [[A]] wrapped in an [[Option]]. If the rule
   * produces [[Some(action)]], it has fired; otherwise, it defers to the next rule.
   *
   * @param F
   *   The effect type (e.g., [[Id]], [[IO]]).
   * @param I
   *   The input type.
   * @param A
   *   The output type.
   */
  type Rule[F[_], I, A] = Kleisli[F, I, Option[A]]

  /**
   * Shorthand for a pure rule (no effect).
   *
   * A rule that operates without any effect, using the [[Id]] type.
   *
   * @param I
   *   The input type.
   * @param A
   *   The output type.
   */
  type RuleId[I, A] = Rule[Id, I, A]

  /**
   * Boolean predicate on the policy input.
   *
   * Represents a condition that evaluates to `true` or `false` based on the input [[I]].
   *
   * @param I
   *   The input type.
   */
  type Condition[I] = I => Boolean

  /**
   * Constant predicate that is always `true`.
   */
  val always: Condition[Any] = _ => true
end BehaviorTypes
