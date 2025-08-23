package io.github.srs.utils

import java.util.UUID

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.ModelModule
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, Wheel as ActWheel }
import io.github.srs.model.illumination.LightMap
import io.github.srs.model.illumination.engine.SquidLibFovEngine
import io.github.srs.model.illumination.model.ScaleFactor
import io.github.srs.model.entity.dynamicentity.sensor.{ ProximitySensor, Sensor }
import io.github.srs.model.entity.dynamicentity.sensor.LightSensor
import io.github.srs.model.entity.dynamicentity.behavior.Policy

object SimulationDefaults:

  val duration: Option[Long] = None
  val seed: Option[Long] = None
  val debugMode = true
  val binarySearchDurationThreshold: FiniteDuration = 1.microseconds
  val lightMap: LightMap[IO] = LightMap.create[IO](SquidLibFovEngine, ScaleFactor.default).unsafeRunSync()

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
      val defaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultWidth: Double = 1.0
      val defaultHeight: Double = 1.0

    object Light:
      val defaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
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

      val defaultMaxRetries = 10

      val defaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
      val defaultPosition: Point2D = (0.0, 0.0)
      val defaultShape: ShapeType.Circle = ShapeType.Circle(0.5)
      val defaultOrientation: Orientation = Orientation(0.0)
      val defaultActuators: Seq[Actuator[Robot]] = Seq.empty
      val defaultSensors: Vector[Sensor[Robot, ModelModule.State]] = Vector.empty

      val stdProximitySensors: Vector[Sensor[Robot, ModelModule.State]] = Vector(
        ProximitySensor(Orientation(0.0), radius),
        ProximitySensor(Orientation(45.0), radius),
        ProximitySensor(Orientation(90.0), radius),
        ProximitySensor(Orientation(135.0), radius),
        ProximitySensor(Orientation(180.0), radius),
        ProximitySensor(Orientation(225.0), radius),
        ProximitySensor(Orientation(270.0), radius),
        ProximitySensor(Orientation(315.0), radius),
      )

      val stdLightSensors: Vector[Sensor[Robot, ModelModule.State]] = Vector(
        LightSensor(Orientation(0.0)),
        LightSensor(Orientation(45.0)),
        LightSensor(Orientation(90.0)),
        LightSensor(Orientation(135.0)),
        LightSensor(Orientation(180.0)),
        LightSensor(Orientation(225.0)),
        LightSensor(Orientation(270.0)),
        LightSensor(Orientation(315.0)),
      )
      val defaultPolicy: Policy = Policy.AlwaysForward
    end Robot
  end DynamicEntity

  object Fields:

    object Simulation:
      val self: String = "simulation"
      val duration: String = "duration"
      val seed: String = "seed"

    object Environment:
      val self: String = "environment"
      val width: String = "width"
      val height: String = "height"
      val entities: String = "entities"

    object Entity:
      val id: String = "id"
      val position: String = "position"
      val x: String = "x"
      val y: String = "y"
      val orientation: String = "orientation"

      object StaticEntity:

        object Obstacle:
          val self: String = "obstacle"
          val width: String = "width"
          val height: String = "height"

        object Light:
          val self: String = "light"
          val radius: String = "radius"
          val illuminationRadius: String = "illuminationRadius"
          val intensity: String = "intensity"
          val attenuation: String = "attenuation"

      object DynamicEntity:

        object Robot:
          val self: String = "robot"
          val radius: String = "radius"
          val speed: String = "speed"
          val withProximitySensors: String = "withProximitySensors"
          val withLightSensors: String = "withLightSensors"
          val behavior: String = "behavior"
    end Entity
  end Fields
end SimulationDefaults
