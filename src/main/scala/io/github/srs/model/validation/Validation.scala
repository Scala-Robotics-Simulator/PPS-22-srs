package io.github.srs.model.validation

enum DomainError:
  case NegativeOrZero(field: String, value: Double)

type Validation[+A] = Either[DomainError, A]

object Validation:
  import scala.math.Numeric
  def positive[T](field: String, v: T)(using n: Numeric[T]): Validation[T] =
    Either.cond(
      n.gt(v, n.zero),
      v,
      DomainError.NegativeOrZero(field, n.toDouble(v))
    )