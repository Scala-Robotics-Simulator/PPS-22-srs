package io.github.srs.model

object ModelModule:

  trait State

  trait Model[S <: State]:
    def update(s: S): Option[S]

  trait Provider[S <: State]:
    val model: Model[S]

  trait Component[S <: State]:

    object Model:
      def apply(updateFunc: S => Option[S]): Model[S] = new ModelImpl(updateFunc)

      private class ModelImpl(updateFunc: S => Option[S]) extends Model[S]:
        override def update(s: S): Option[S] = updateFunc(s)

  trait Interface[S <: State] extends Provider[S] with Component[S]
