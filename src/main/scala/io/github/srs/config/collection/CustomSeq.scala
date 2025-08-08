package io.github.srs.config.collection

import io.github.srs.config.{ ConfigError, ConfigResult }

object CustomSeq:

  extension [A](seq: Seq[ConfigResult[A]])

    /**
     * Utility method to sequence a collection of `ConfigResult`s into a single `ConfigResult` containing a sequence of
     * results.
     * @return
     *   a `ConfigResult` containing a sequence of results if all are successful, or a sequence of errors if any fail.
     */
    def sequence: ConfigResult[Seq[A]] =
      seq.foldRight(Right[Seq[ConfigError], Seq[A]](Nil): ConfigResult[Seq[A]]):
        case (Right(a), Right(as)) => Right[Seq[ConfigError], Seq[A]](a +: as)
        case (Left(e1), Right(_)) => Left[Seq[ConfigError], Seq[A]](e1)
        case (Right(_), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e2)
        case (Left(e1), Left(e2)) => Left[Seq[ConfigError], Seq[A]](e1 ++ e2)
