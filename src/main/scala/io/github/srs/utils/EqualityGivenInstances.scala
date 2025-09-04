package io.github.srs.utils

object EqualityGivenInstances:
  inline given [T]: CanEqual[T, T] = CanEqual.derived
