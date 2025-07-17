package io.github.srs.model.validation

/**
 * ADT collecting validation failures that are meaningful in the domain.
 */
enum DomainError:
  case NegativeOrZero(field: String, value: Double)

/**
 * Type alias for domain‑level validations: `Right` = valid, `Left` = error.
 */
type Validation[+A] = Either[DomainError, A]

/**
 * Companion object for [[Validation]] that provides utility methods for common validations.
 */
object Validation:
  import scala.math.Numeric

  /**
   * Ensures the given numeric value is strictly positive.
   *
   * @param field
   *   name of the validated field (for error reporting)
   * @param v
   *   numeric value to check
   * @tparam T
   *   any [[Numeric]] type (e.g., `Int`, `Double`, etc.)
   * @return
   *   [[Right]] with the value if it is positive, otherwise [[Left]] with a [[DomainError.NegativeOrZero]] error.
   *
   * @example
   *   {{{
   * import io.github.srs.model.validation.Validation.*
   *
   * positive("width", 10)   // Right(10)
   * positive("width", 0)    // Left(NegativeOrZero("width", 0.0))
   *   }}}
   */
  def positive[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gt(v, n.zero), // predicate
      v, // happy path
      DomainError.NegativeOrZero(field, n.toDouble(v)), // error
    )
end Validation
