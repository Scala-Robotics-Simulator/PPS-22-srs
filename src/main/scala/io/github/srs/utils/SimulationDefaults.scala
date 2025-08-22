package io.github.srs.utils

import java.awt.Color
import java.util.UUID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, Wheel as ActWheel }
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment

object SimulationDefaults:

  object UI:

    object SimulationViewConstants:
      val IdDisplayLength = 8
      val PositionDecimals = 2
      val OrientationDecimals = 1
      val DefaultRobotInfo = "Select a robot to view details"
      val StopConfirmMessage = "Are you sure you want to stop the simulation?\n\nClick Yes to stop, No to continue."
      val StopConfirmTitle = "Stop Simulation"

    object Colors:
      @inline private def rgb(r: Int, g: Int, b: Int) = new java.awt.Color(r, g, b)
      @inline private def rgba(r: Int, g: Int, b: Int, a: Int) = new java.awt.Color(r, g, b, a)
      def backgroundLight: Color = rgb(250, 250, 250)
      def backgroundMedium: Color = rgb(245, 245, 245)
      def border: Color = rgb(200, 200, 200)
      def text: Color = rgb(60, 60, 60)
      def obstacleGradientStart: Color = rgb(120, 120, 120)
      def obstacleGradientEnd: Color = rgb(80, 80, 80)
      def obstacleBorder: Color = rgb(60, 60, 60)
      def robotDefault: Color = rgb(100, 150, 255)
      def robotDefaultDark: Color = rgb(50, 100, 200)
      def robotDefaultBorder: Color = rgb(0, 50, 150)
      def robotSelected: Color = rgb(255, 100, 100)
      def robotSelectedDark: Color = rgb(200, 50, 50)
      def robotSelectedBorder: Color = rgb(150, 0, 0)
      def robotShadow: Color = rgba(0, 0, 0, 50)
      def lightCenter: Color = rgba(255, 255, 200, 200)
      def lightEdge: Color = rgba(255, 140, 0, 80)
      def buttonHover: Color = rgb(230, 230, 230)
      def buttonPressed: Color = rgb(220, 235, 250)

    end Colors

    object Fonts:
      val family = "Arial"
      val titleSize = 12

    object Spacing:
      val standardPadding = 10
      val innerPadding = 5
      val componentGap = 10

    object Dimensions:
      val buttonWidth = 150
      val buttonHeight = 30
      val robotListWidth = 250
      val robotListHeight = 300
      val infoAreaRows = 6
      val infoAreaColumns = 25

    object Strokes:
      val obstacleStroke = 1.5f
      val robotShadowStroke = 3f

    object Icons:
      val play = "\u25B6"
      val stop = "\u23F9"
      val pause = "\u23F8"
  end UI

  val duration: Option[Long] = None
  val seed: Option[Long] = None
  val debugMode = true
  val binarySearchDurationThreshold: FiniteDuration = 1.microseconds

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
      val defaultSensors: Vector[Sensor[Robot, Environment]] = Vector.empty

      val selectionStroke: Float = 3f
      val normalStroke: Float = 1f
      val arrowLengthFactor: Double = 0.6
      val arrowWidthFactor: Double = 0.3
      val minArrowWidth: Float = 2f

      val stdProximitySensors: Vector[Sensor[Robot, Environment]] = Vector(
        ProximitySensor(Orientation(0.0), radius),
        ProximitySensor(Orientation(45.0), radius),
        ProximitySensor(Orientation(90.0), radius),
        ProximitySensor(Orientation(135.0), radius),
        ProximitySensor(Orientation(180.0), radius),
        ProximitySensor(Orientation(225.0), radius),
        ProximitySensor(Orientation(270.0), radius),
        ProximitySensor(Orientation(315.0), radius),
      )

      val stdLightSensors: Vector[Sensor[Robot, Environment]] = Vector(
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

  object Layout:
    val splitPaneWeight: Double = 0.8
    val splitPaneLocation: Double = 0.8

  object Frame:
    val minWidth = 800
    val minHeight = 600
    val prefWidth = 1200
    val prefHeight = 720
    val splitWeight = 0.8
    val canvasBorder: Int = 2

  object Canvas:
    val borderSize = 2
    val minZoom = 0.2
    val maxZoom = 5.0
    val zoomInFactor = 1.2
    val zoomOutFactor = 0.8
    val desiredLabelPixels = 40.0
    val gridStrokeWidth = 1f
    val labelDesiredPx: Double = 40.0
    val minLightSize: Int = 12
    val lightStroke: Float = 2f
    val labelBottomOffset: Int = 4
    val labelYOffset: Int = 12
    val labelXOffset: Int = 2

  object ControlsPanel:
    val startStopButtonText: String = "Start/Stop"

end SimulationDefaults
