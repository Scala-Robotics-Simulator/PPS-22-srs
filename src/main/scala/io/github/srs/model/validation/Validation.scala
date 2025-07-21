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

  def positiveWithZero[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gteq(v, n.zero),
      v,
      DomainError.Negative(field, n.toDouble(v)),
    )

  private def bounded[T](field: String, v: T, min: T, max: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gteq(v, min) && n.lt(v, max),
      v,
      DomainError.OutOfBounds(field, n.toDouble(v), n.toDouble(min), n.toDouble(max)),
    )

  def notNaN[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      !n.toDouble(v).isNaN,
      v,
      DomainError.NotANumber(field, n.toDouble(v)),
    )

  def notInfinite[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      !n.toDouble(v).isInfinite,
      v,
      DomainError.Infinite(field, n.toDouble(v)),
    )

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
