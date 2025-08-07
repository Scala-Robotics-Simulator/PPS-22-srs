package io.github.srs.utils.chaining

/**
 * A utility object providing a pipe operator for chaining function calls.
 *
 * The pipe operator `|>` allows you to pass a value through a series of functions in a more readable way.
 */
object Pipe:

  given pipeOps: AnyRef with

    extension [A](value: A)
      /**
       * The pipe operator `|>` allows you to pass a value through a function. This operator is useful for chaining
       * function calls in a more readable way.
       * @param f
       *   the function to apply to the value
       * @return
       *   the result of applying the function `f` to the value
       */
      inline def |>[B](f: A => B): B = f(value)
