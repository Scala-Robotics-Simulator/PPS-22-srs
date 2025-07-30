package io.github.srs.utils

object SimulationDefaults:

  object Environment:
    val minWidth: Int = 1
    val maxWidth: Int = 500

    val minHeight: Int = 1
    val maxHeight: Int = 500

    val maxEntities: Int = 200

  object StaticEntity:

    object Light:
      val radius: Double = 0.05

  object DynamicEntity:
    val minSpeed: Double = -1.0
    val maxSpeed: Double = 1.0
