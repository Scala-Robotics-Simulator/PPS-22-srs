package io.github.srs.model.behavior

opaque type Behavior[-I,+A] = I => Seq[A]

object Behavior:

  inline def apply[I, A](f: I => Seq[A]): Behavior[I, A] = (in: I) => f(in)
  inline def pure[I, A](a: A): Behavior[I, A] = (_: I) => Seq(a)
  def empty[I, A]: Behavior[I, A] = (_: I) => Seq.empty
  def when[I, A](p: I => Boolean)(actions: => Seq[A]): Behavior[I, A] =
    (in: I) => if p(in) then actions else Seq.empty

  extension [I, A](self: Behavior[I, A])
    inline def execute(in: I): Seq[A] = self(in)
