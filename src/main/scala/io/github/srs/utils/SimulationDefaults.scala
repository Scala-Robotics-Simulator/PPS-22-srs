package io.github.srs.utils

import cats.effect.IO
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.action.Action
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, Wheel as ActWheel }
import io.github.srs.model.entity.dynamicentity.behavior.BehaviorTypes.Rule
import io.github.srs.model.entity.dynamicentity.behavior.Rules
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor, SensorReadings }
import io.github.srs.model.environment.Environment

object SimulationDefaults:

  val duration: Option[Int] = None
  val seed: Option[Long] = None

  object SimulationConfig:
    val maxCount = 10_000

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
    val zeroSpeed: Double = 0.0
    val minSpeed: Double = -1.0
    val maxSpeed: Double = 1.0
    val halfSpeed: Double = 0.5

    object Actuator:

      object DifferentialWheelMotor:
        val defaultWheel: ActWheel = ActWheel()

        object Wheel:
          val defaultSpeed: Double = 1.0
          val defaultShape: ShapeType.Circle = ShapeType.Circle(0.1)

    object Sensor:

      object ProximitySensor:
        val defaultOffset: Double = 0.0
        val defaultDistance: Double = 0.5
        val defaultRange: Double = 5.0

    object Robot:
      import SimulationDefaults.DynamicEntity.Robot.defaultShape.radius

      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultShape: ShapeType.Circle = ShapeType.Circle(0.5)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultActuators: Seq[Actuator[Robot]] = Seq.empty
      val defaultSensors: Vector[Sensor[Robot, Environment]] = Vector.empty

      val stdProximitySensors: Vector[Sensor[Robot, Environment]] = Vector(
        ProximitySensor(Orientation(0.0), radius, 5.0),
        ProximitySensor(Orientation(45.0), radius, 5.0),
        ProximitySensor(Orientation(90.0), radius, 5.0),
        ProximitySensor(Orientation(135.0), radius, 5.0),
        ProximitySensor(Orientation(180.0), radius, 5.0),
        ProximitySensor(Orientation(225.0), radius, 5.0),
        ProximitySensor(Orientation(270.0), radius, 5.0),
        ProximitySensor(Orientation(315.0), radius, 5.0),
      )

      // TODO: Add light sensors when implemented
      val stdLightSensors: Vector[Sensor[Robot, Environment]] = Vector.empty

      val defaultBehavior: Rule[IO, SensorReadings, Action[IO]] = Rules.alwaysForward[IO]
    end Robot
  end DynamicEntity
end SimulationDefaults
