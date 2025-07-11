package io.github.srs.model

object ModelModule:

  trait Model:
    def getData: Int

  trait Provider:
    val model: Model

  trait Component:

    object Model:
      def apply(): Model = new ModelImpl()

      private class ModelImpl extends Model:
        def getData: Int = 1

  trait Interface extends Provider with Component
