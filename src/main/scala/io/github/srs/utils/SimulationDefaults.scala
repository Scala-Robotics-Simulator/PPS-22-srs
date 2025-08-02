package io.github.srs.utils

import io.github.srs.model.entity.{ Entity, Orientation, Point2D }

object SimulationDefaults:

  object Environment:
    val defaultWidth: Int = 10
    val minWidth: Int = 1
    val maxWidth: Int = 500

    val defaultHeight: Int = 10
    val minHeight: Int = 1
    val maxHeight: Int = 500

    val defaultEntities: Set[Entity] = Set.empty
    val maxEntities: Int = 200

  object StaticEntity:

    object Obstacle:
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultWidth: Double = 1.0
      val defaultHeight: Double = 1.0

    object Light:
      val radius: Double = 0.05

  object DynamicEntity:
    val minSpeed: Double = -1.0
    val maxSpeed: Double = 1.0
end SimulationDefaults
