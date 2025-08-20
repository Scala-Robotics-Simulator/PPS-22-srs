package io.github.srs.model.entity.dynamicentity.behavior.dsl

import scala.annotation.targetName

import cats.data.Kleisli
import io.github.srs.model.entity.dynamicentity.*
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.{ Behavior, Condition, PartialBehavior }

/**
 * # Behavior DSL (pure)
 *
 * Minimal DSL to build decision logic:
 *
 *   - [[PartialBehavior]] = `Kleisli[Option, I, A]` – a partial decision (“may produce”).
 *   - [[Behavior]] = `Kleisli[Id, I, A]` – a total decision (“always produces”).
 *
 * Partial behaviors compose **left-biased** (the first `Some` wins) and are finalized into total behaviors via
 * [[orElse]] (or the compatibility alias [[default]]).
 *
 * @example
 *   {{{
 * val isEven: Condition[Int] = _ % 2 == 0
 *
 * val rule: PartialBehavior[Int, String] =
 *   (isEven ==> "even") | (_ => true) ==> "odd"
 *
 * val behavior: Behavior[Int, String] =
 *   rule.orElse("n/a")
 *
 * behavior.run(2)  // "even"
 * behavior.run(3)  // "odd"
 *   }}}
 */
object BehaviorDsl:

  extension [I, A](cond: Condition[I])

    /**
     * Build a partial behavior from a condition.
     *
     * Produces `Some(act)` when the condition holds; otherwise `None`. The action is call-by-name and evaluated only if
     * needed.
     *
     * @param act
     *   action to produce when the condition holds (lazy)
     * @return
     *   a [[PartialBehavior]] that yields `Some(act)` if `cond(i)` is true, else `None`
     */
    @targetName("implies")
    infix def ==>(act: => A): PartialBehavior[I, A] =
      Kleisli(i => if cond(i) then Some(act) else None)

  extension [I, A](r1: PartialBehavior[I, A])

    /**
     * Left-biased composition of two partial behaviors.
     *
     * Evaluates `r1(i)`: if it returns `Some(a)` the result is kept, otherwise `r2(i)` is evaluated.
     *
     * @param r2
     *   fallback partial behavior to try when `r1` defers
     * @return
     *   a [[PartialBehavior]] that prefers `r1` over `r2`
     */
    @targetName("orElsePartial")
    infix def |(r2: PartialBehavior[I, A]): PartialBehavior[I, A] =
      Kleisli(i => r1.run(i).orElse(r2.run(i)))

    /**
     * Finalize a partial behavior into a total behavior by providing a fallback.
     *
     * If the composed partial chain yields `None`, the fallback is used.
     *
     * @param fallback
     *   default action to use when no rule fires (lazy)
     * @return
     *   a total [[Behavior]] that always produces an action
     */
    def orElse(fallback: => A): Behavior[I, A] =
      Kleisli(i => r1.run(i).getOrElse(fallback))

    /**
     * Compatibility alias for [[orElse]].
     *
     * @param fallback
     *   default action to use when no rule fires (lazy)
     * @return
     *   a total [[Behavior]] that always produces an action
     */
    def default(fallback: => A): Behavior[I, A] = orElse(fallback)

    /**
     * Gate this partial behavior with an additional predicate.
     *
     * When `p(i)` is false, this rule forcibly defers (`None`).
     *
     * @param p
     *   additional predicate on the same input
     * @return
     *   a [[PartialBehavior]] that runs only if `p` holds
     */
    def onlyIf(p: Condition[I]): PartialBehavior[I, A] =
      Kleisli(i => if p(i) then r1.run(i) else None)

  end extension

  extension [I](c: Condition[I])

    /**
     * Logical AND of two conditions.
     *
     * @param d
     *   condition to combine with
     * @return
     *   a condition that holds only if both `c` and `d` hold
     */
    infix def and(d: Condition[I]): Condition[I] = i => c(i) && d(i)

    /**
     * Logical OR of two conditions.
     *
     * @param d
     *   condition to combine with
     * @return
     *   a condition that holds if either `c` or `d` holds
     */
    infix def or(d: Condition[I]): Condition[I] = i => c(i) || d(i)

    /**
     * Logical NOT of a condition.
     *
     * @return
     *   a condition that holds when `c` does not hold
     */
    def not: Condition[I] = i => !c(i)
  end extension
end BehaviorDsl
