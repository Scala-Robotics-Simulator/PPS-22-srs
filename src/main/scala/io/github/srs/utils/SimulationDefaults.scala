package io.github.srs.utils

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.sensor.Sensor
import io.github.srs.model.entity.dynamicentity.{ Actuator, Robot }
import io.github.srs.model.environment.Environment

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
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultRadius: Double = 0.05
      val defaultIlluminationRadius: Double = 1.0
      val defaultIntensity: Double = 1.0
      val defaultAttenuation: Double = 1.0

    object Boundary:
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultWidth: Double = 1.0
      val defaultHeight: Double = 1.0

  end StaticEntity

  object DynamicEntity:
    val minSpeed: Double = -1.0
    val maxSpeed: Double = 1.0

    object Robot:
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultShape: ShapeType.Circle = ShapeType.Circle(0.5)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultActuators: Seq[Actuator[Robot]] = Seq.empty
      val defaultSensors: Vector[Sensor[Robot, Environment]] = Vector.empty
end SimulationDefaults
