package io.github.srs.model.behavior

import scala.annotation.targetName

/**
 * A Behavior is a function that takes an input of type `I` and produces a sequence of actions of type `A`.
 *
 * @param I
 *   the input type, which can be any type that provides context for the behavior
 * @param A
 *   the output/action type, which can be any type that represents an action or effect
 */
opaque type Behavior[-I, +A] = I => Seq[A]

/**
 * Constructors and combinators for creating and combining behaviors.
 */
object Behavior:

  /**
   * Lift an existing function into a [[Behavior]].
   *
   * @param f
   *   a function from `I` to [[Seq[A]]]
   * @tparam I
   *   the input type
   * @tparam A
   *   the output/action type
   * @return
   *   a [[Behavior]] wrapping `f`
   */
  inline def apply[I, A](f: I => Seq[A]): Behavior[I, A] = (in: I) => f(in)

  /**
   * Create a [[Behavior]] that always returns the same action.
   *
   * @param a
   *   the action to return
   * @tparam I
   *   the input type
   * @tparam A
   *   the output/action type
   * @return
   *   a [[Behavior]] that always returns `a`
   */
  inline def pure[I, A](a: A): Behavior[I, A] = (_: I) => Seq(a)

  /**
   * Create a [[Behavior]] that produces no actions.
   *
   * @tparam I
   *   the input type
   * @tparam A
   *   the output/action type
   * @return
   *   a [[Behavior]] that produces an empty sequence of actions
   */
  def empty[I, A]: Behavior[I, A] = (_: I) => Seq.empty

  /**
   * Create a [[Behavior]] that executes actions based on a predicate.
   *
   * @param p
   *   a predicate function that takes an input of type `I` and returns a Boolean
   * @param actions
   *   the actions to execute if the predicate is true
   * @tparam I
   *   the input type
   * @tparam A
   *   the output/action type
   * @return
   *   a [[Behavior]] that executes `actions` if `p(in)` is true, otherwise produces no actions
   */
  def when[I, A](p: I => Boolean)(actions: => Seq[A]): Behavior[I, A] =
    (in: I) => if p(in) then actions else Seq.empty

  /**
   * Extension methods for the `[[Behavior]]` for manipulation and execution.
   */
  extension [I, A](self: Behavior[I, A])
    /**
     * Execute the behavior with the given input.
     *
     * @param in
     *   the input to the behavior
     * @return
     *   a sequence of actions produced by the behavior
     */
    inline def execute(in: I): Seq[A] = self(in)
    def map[B](f: A => B): Behavior[I, B] = (i: I) => self(i).map(f)

    /**
     * Filter the actions produced by the behavior based on a predicate.
     * @param p
     *   the predicate function to filter actions
     * @return
     *   a new Behavior that only produces actions satisfying the predicate
     */
    def filter(p: A => Boolean): Behavior[I, A] = (i: I) => self(i).filter(p)

    /**
     * Combine two behaviors into one.
     *
     * @param that
     *   the other behavior to combine with
     * @return
     *   a new [[Behavior]] that produces actions from both behaviors
     */
    @targetName("concat")
    infix def ++(that: Behavior[I, A]): Behavior[I, A] =
      (i: I) => self(i) ++ that(i)

    /**
     * Alias for `++`, providing a more readable syntax for combining behaviors.
     * @param that
     *   the other behavior to combine with
     * @return
     *   a new [[Behavior]] that produces actions from both behaviors
     */
    infix def andAlso[B](that: Behavior[I, A]): Behavior[I, A] = (i: I) => self(i) ++ that(i)

    /**
     * Combine two behaviors such that the second behavior is executed only if the first produces no actions.
     *
     * @param that
     *   the other behavior to combine with
     * @return
     *   a new [[Behavior]] that produces actions from the first behavior, or from the second if the first is empty
     */
    @targetName("alt")
    infix def <|>(that: Behavior[I, A]): Behavior[I, A] =
      (i: I) =>
        val as = self(i)
        if as.nonEmpty then as else that(i)

    /**
     * Alias for `<|>`, providing a more readable syntax for combining behaviors.
     * @param that
     *   the other behavior to combine with
     * @return
     *   a new [[Behavior]] that produces actions from the first behavior, or from the second if the first is empty
     */
    infix def orElse(that: Behavior[I, A]): Behavior[I, A] = self <|> that
  end extension
end Behavior
