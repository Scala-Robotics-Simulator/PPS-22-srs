package io.github.srs.utils

import io.github.srs.model.entity.Entity

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

    object Light:
      val radius: Double = 0.05

  object DynamicEntity:
    val minSpeed: Double = -1.0
    val maxSpeed: Double = 1.0
end SimulationDefaults
