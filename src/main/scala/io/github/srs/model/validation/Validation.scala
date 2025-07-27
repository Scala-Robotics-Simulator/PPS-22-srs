package io.github.srs.model.validation

import scala.reflect.ClassTag

/**
 * ADT collecting validation failures that are meaningful in the domain.
 */
enum DomainError:
  case Negative(field: String, value: Double)
  case NegativeOrZero(field: String, value: Double)
  case OutOfBounds(field: String, value: Double, min: Double, max: Double)
  case NotANumber(field: String, value: Double)
  case Infinite(field: String, value: Double)
  case InvalidCount(field: String, count: Int, min: Int, max: Int)

/**
 * Companion object for [[DomainError]] that provides an extension method to get a human-readable error message.
 */
object DomainError:

  extension (e: DomainError)

    def errorMessage: String = e match
      case Negative(f, v) => s"$f is negative ($v)"
      case NegativeOrZero(f, v) => s"$f is ≤ 0 ($v)"
      case OutOfBounds(f, v, lo, hi) => s"$f = $v is outside [$lo, $hi)"
      case NotANumber(f, _) => s"$f is NaN"
      case Infinite(f, _) => s"$f is infinite"
      case InvalidCount(f, c, lo, hi) => s"$f has $c elements, allowed $lo $hi"

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

  /**
   * Ensures the given numeric value is non-negative, the value can be zero.
   * @param field
   *   the name of the field being validated.
   * @param v
   *   the numeric value to validate.
   * @param n
   *   the numeric type class instance for the type of `v`.
   * @tparam T
   *   the [[Numeric]] type.
   * @return
   *   [[Right]] with the value if it is non-negative, otherwise [[Left]] with a [[DomainError.Negative]] error.
   */
  def positiveWithZero[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gteq(v, n.zero),
      v,
      DomainError.Negative(field, n.toDouble(v)),
    )

  /**
   * Ensures the given numeric value is within a specified range (inclusive of min, exclusive of max).
   * @param field
   *   the name of the field being validated.
   * @param v
   *   the numeric value to validate.
   * @param min
   *   the minimum value (inclusive).
   * @param max
   *   the maximum value (exclusive).
   * @param n
   *   the numeric type class instance for the type of `v`.
   * @tparam T
   *   the [[Numeric]] type.
   * @return
   *   [[Right]] with the value if it is within the bounds, otherwise [[Left]] with a [[DomainError.OutOfBounds]] error.
   */
  private def bounded[T](field: String, v: T, min: T, max: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gteq(v, min) && n.lt(v, max),
      v,
      DomainError.OutOfBounds(field, n.toDouble(v), n.toDouble(min), n.toDouble(max)),
    )

  /**
   * Ensures the given numeric value is not NaN (Not a Number).
   * @param field
   *   the name of the field being validated.
   * @param v
   *   the numeric value to validate.
   * @param n
   *   the numeric type class instance for the type of `v`.
   * @tparam T
   *   the [[Numeric]] type.
   * @return
   *   [[Right]] with the value if it is not NaN, otherwise [[Left]] with a [[DomainError.NotANumber]] error.
   */
  def notNaN[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      !n.toDouble(v).isNaN,
      v,
      DomainError.NotANumber(field, n.toDouble(v)),
    )

  /**
   * Ensures the given numeric value is not infinite.
   * @param field
   *   the name of the field being validated.
   * @param v
   *   the numeric value to validate.
   * @param n
   *   the numeric type class instance for the type of `v`.
   * @tparam T
   *   the [[Numeric]] type.
   * @return
   *   [[Right]] with the value if it is not infinite, otherwise [[Left]] with a [[DomainError.Infinite]] error.
   */
  def notInfinite[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      !n.toDouble(v).isInfinite,
      v,
      DomainError.Infinite(field, n.toDouble(v)),
    )

  /**
   * Ensures the count of elements of a specific type in a sequence is within a specified range.
   * @param field
   *   the name of the field being validated.
   * @param elements
   *   the sequence of elements to validate.
   * @param min
   *   the minimum count of elements of type `A` (inclusive).
   * @param max
   *   the maximum count of elements of type `A` (inclusive).
   * @tparam A
   *   the type of elements to count (must be a subtype of `A`).
   * @return
   *   [[Right]] with the original sequence if the count is within bounds, otherwise [[Left]] with a
   *   [[DomainError.InvalidCount]] error.
   */
  def validateCountOfType[A: ClassTag](
      field: String,
      elements: Seq[?],
      min: Int,
      max: Int,
  ): Validation[Seq[?]] =
    val matched = elements.collect { case a: A => a }
    val count = matched.size
    bounded("count", count, min, max + 1).map { _ =>
      elements
    }.left.map { _ =>
      DomainError.InvalidCount(field, count, min, max)
    }

end Validation
