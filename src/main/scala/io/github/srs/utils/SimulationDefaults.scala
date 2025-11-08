package io.github.srs.utils

import java.awt.Color
import java.util.UUID

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.model.entity.*
import io.github.srs.model.entity.dynamicentity.actuator.{ Actuator, Wheel as ActWheel }
import io.github.srs.model.entity.dynamicentity.agent.Agent
import io.github.srs.model.entity.dynamicentity.agent.reward.Reward
import io.github.srs.model.entity.dynamicentity.robot.Robot
import io.github.srs.model.entity.dynamicentity.robot.behavior.Policy
import io.github.srs.model.entity.dynamicentity.sensor.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.illumination.LightMap
import io.github.srs.model.illumination.engine.SquidLibFovEngine
import io.github.srs.model.illumination.model.ScaleFactor
import io.github.srs.model.entity.dynamicentity.agent.termination.Termination
import io.github.srs.model.entity.dynamicentity.agent.truncation.Truncation

object SimulationDefaults:

  object Illumination:

    val GridThreshold = 10_000
    val LightThreshold = 2

    object Occlusion:
      val FullRotation: Double = 90.0
      val AlmostZero: Double = 1e-6

  object Behaviors:

    object Prioritized:
      val DangerDist: Double = 0.40
      val LightThreshold: Double = 0.05

    object Phototaxis:
      val Epsilon: Double = 1e-9
      val MinForwardBias: Double = 0.4
      val TurnGain: Double = 1.0

    object ObstacleAvoidance:
      val CruiseSpeed: Double = 0.35
      val WarnSpeed: Double = 0.15
      val WarnTurnSpeed: Double = 0.55
      val BackBoost: Double = 0.20
      val SafeDist: Double = 0.5
      val CriticalDist: Double = 0.35

    object RandomWalk:
      val MinForwardFactor: Double = 0.35
      val MaxForwardExtra: Double = 0.35
      val MinTurnOfBase: Double = 0.35
      val MaxTurnOfBase: Double = 1.15
      val TurnExponent: Double = 1.2
      val PivotBoostProb: Double = 0.20
      val PivotBoostAbs: Double = 0.15

  end Behaviors

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
      def timeDisplay: Color = rgb(50, 50, 50)

    end Colors

    object Fonts:
      val FontSize = 12
      val TitleSize = 12

    object Spacing:
      val StandardPadding = 10
      val InnerPadding = 5
      val ComponentGap = 10

    object Dimensions:
      val ButtonWidth = 150
      val ButtonHeight = 30
      val RobotListWidth = 250
      val RobotListHeight = 300
      val InfoAreaRows = 6
      val InfoAreaColumns = 25

    object Strokes:
      val ObstacleStroke = 1.5f
      val RobotShadowStroke = 3f

    object Icons:
      val Play = "\u25B6"
      val Stop = "\u23F9"
      val Pause = "\u23F8"
  end UI

  val Duration: Option[Long] = None
  val Seed: Option[Long] = None
  val DebugMode = false
  val BinarySearchDurationThreshold: FiniteDuration = 1.microseconds

  /**
   * Alternative light map configurations for different use cases
   */
  object LightMapConfigs:

    /**
     * Default light map with caching enabled Uses scale factor 10 for a good balance of performance and precision
     */
    lazy val BaseLightMap: LightMap[IO] =
      LightMap
        .create[IO](ScaleFactor.default, SquidLibFovEngine)
        .unsafeRunSync()

    /**
     * High-precision light map for detailed rendering Uses scale factor 100 for maximum precision.
     *
     * @return
     *   A [[io.github.srs.model.illumination.LightMap]] configured for high precision, or the default light map if the
     *   scale factor is invalid.
     */
    def HPLightMap: LightMap[IO] =
      ScaleFactor
        .validate(80)
        .map { scale =>
          LightMap
            .create[IO](scale, SquidLibFovEngine)
            .unsafeRunSync()
        }
        .getOrElse(BaseLightMap)

    /**
     * Fast light map for real-time simulation Uses scale factor 5 for maximum performance.
     *
     * @return
     *   A [[io.github.srs.model.illumination.LightMap]] configured for fast computation, or the default light map if
     *   the scale factor is invalid.
     */
    def fastLightMap: LightMap[IO] =
      ScaleFactor
        .validate(10)
        .map { scale =>
          LightMap
            .create[IO](scale, SquidLibFovEngine)
            .unsafeRunSync()
        }
        .getOrElse(BaseLightMap)

    /**
     * Custom light map with specific scale factor Curried for better composition.
     *
     * @param scaleFactor
     *   The desired scale factor (cells per meter).
     * @return
     *   A [[io.github.srs.model.illumination.LightMap]] configured with the specified scale factor, or the default
     *   light map if the scale factor is invalid.
     */
    def withScale(scaleFactor: Int): LightMap[IO] =
      ScaleFactor
        .validate(scaleFactor)
        .map { scale =>
          LightMap
            .create[IO](scale, SquidLibFovEngine)
            .unsafeRunSync()
        }
        .getOrElse(BaseLightMap)

  end LightMapConfigs

  object SimulationConfig:
    val MaxCount = 10_000

  object Environment:
    val DefaultWidth: Int = 10
    val MinWidth: Int = 1
    val MaxWidth: Int = 500

    val DefaultHeight: Int = 10
    val MinHeight: Int = 1
    val MaxHeight: Int = 500

    val DefaultEntities: List[Entity] = List.empty
    val MaxEntities: Int = 50

  object StaticEntity:

    object Obstacle:
      val DefaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
      val DefaultPosition: Point2D = (0.0, 0.0)
      val DefaultOrientation: Orientation = Orientation(0.0)
      val DefaultWidth: Double = 1.0
      val DefaultHeight: Double = 1.0

    object Light:
      val DefaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
      val DefaultPosition: Point2D = (0.0, 0.0)
      val DefaultOrientation: Orientation = Orientation(0.0)
      val DefaultRadius: Double = 0.05
      val DefaultIlluminationRadius: Double = 1.0
      val DefaultIntensity: Double = 1.0
      val DefaultAttenuation: Double = 1.0

    object Boundary:
      val DefaultPosition: Point2D = (0.0, 0.0)
      val DefaultOrientation: Orientation = Orientation(0.0)
      val DefaultWidth: Double = 1.0
      val DefaultHeight: Double = 1.0

  end StaticEntity

  object DynamicEntity:
    val DefaultMaxRetries = 10

    val ZeroSpeed: Double = 0.0
    val MinSpeed: Double = -1.0
    val MaxSpeed: Double = 1.0
    val HalfSpeed: Double = 0.5

    object Actuator:

      object DifferentialWheelMotor:
        val DefaultWheel: ActWheel = ActWheel()

        object Wheel:
          val DefaultSpeed: Double = 1.0
          val DefaultShape: ShapeType.Circle = ShapeType.Circle(0.1)
          val MinSpeed: Double = -1.0
          val MaxSpeed: Double = 1.0

    object Sensor:

      object ProximitySensor:
        val DefaultOffset: Double = 0.0
        val DefaultRange: Double = 1.0

    object Agent:
      val DefaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
      val DefaultPosition: Point2D = (0.0, 0.0)
      val DefaultShape: ShapeType.Circle = ShapeType.Circle(0.25)
      val DefaultOrientation: Orientation = Orientation(0.0)
      val DefaultActuators: Seq[Actuator[Agent]] = Seq.empty
      val DefaultSensors: Vector[Sensor[Agent, Environment]] = Vector.empty
      val MinRadius: Double = 0.01
      val MaxRadius: Double = 0.5
      val DefaultReward: Reward = Reward.NoReward
      val DefaultTermination: Termination = Termination.NeverTerminate
      val DefaultTruncation: Truncation = Truncation.NeverTruncate

      val StdProximitySensors: Vector[Sensor[Agent, Environment]] = Vector(
        ProximitySensor(Orientation(0.0)),
        ProximitySensor(Orientation(45.0)),
        ProximitySensor(Orientation(90.0)),
        ProximitySensor(Orientation(135.0)),
        ProximitySensor(Orientation(180.0)),
        ProximitySensor(Orientation(225.0)),
        ProximitySensor(Orientation(270.0)),
        ProximitySensor(Orientation(315.0)),
      )

      val StdLightSensors: Vector[Sensor[Agent, Environment]] = Vector(
        LightSensor(Orientation(0.0)),
        LightSensor(Orientation(45.0)),
        LightSensor(Orientation(90.0)),
        LightSensor(Orientation(135.0)),
        LightSensor(Orientation(180.0)),
        LightSensor(Orientation(225.0)),
        LightSensor(Orientation(270.0)),
        LightSensor(Orientation(315.0)),
      )

      object CollisionAvoidance:
        val CollisionTriggerDistance: Double = 0.05

      object CoverageTermination:
        val CoverageThreshold: Double = 0.8
        val CellSize: Double = 1.0
        val WindowStuck: Int = 10

    end Agent

    object Robot:

      val DefaultId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
      val DefaultPosition: Point2D = (0.0, 0.0)
      val DefaultShape: ShapeType.Circle = ShapeType.Circle(0.25)
      val DefaultOrientation: Orientation = Orientation(0.0)
      val DefaultActuators: Seq[Actuator[Robot]] = Seq.empty
      val DefaultSensors: Vector[Sensor[Robot, Environment]] = Vector.empty
      val MinRadius: Double = 0.01
      val MaxRadius: Double = 0.5

      val SelectionStroke: Float = 3f
      val NormalStroke: Float = 1f
      val ArrowLengthFactor: Double = 0.6
      val ArrowWidthFactor: Double = 0.3
      val MinArrowWidth: Float = 2f

      val StdProximitySensors: Vector[Sensor[Robot, Environment]] = Vector(
        ProximitySensor(Orientation(0.0)),
        ProximitySensor(Orientation(45.0)),
        ProximitySensor(Orientation(90.0)),
        ProximitySensor(Orientation(135.0)),
        ProximitySensor(Orientation(180.0)),
        ProximitySensor(Orientation(225.0)),
        ProximitySensor(Orientation(270.0)),
        ProximitySensor(Orientation(315.0)),
      )

      val StdLightSensors: Vector[Sensor[Robot, Environment]] = Vector(
        LightSensor(Orientation(0.0)),
        LightSensor(Orientation(45.0)),
        LightSensor(Orientation(90.0)),
        LightSensor(Orientation(135.0)),
        LightSensor(Orientation(180.0)),
        LightSensor(Orientation(225.0)),
        LightSensor(Orientation(270.0)),
        LightSensor(Orientation(315.0)),
      )
      val DefaultPolicy: Policy = Policy.AlwaysForward
    end Robot
  end DynamicEntity

  object GridDSL:
    val ObstacleSize: Double = 0.999999
    val IncrementToCenterPos: Point2D = Point2D(0.5, 0.5)

  object Fields:

    object Simulation:
      val Self: String = "simulation"
      val Duration: String = "duration"
      val Seed: String = "seed"

    object Environment:
      val Self: String = "environment"
      val Width: String = "width"
      val Height: String = "height"
      val Entities: String = "entities"

    object Entity:
      val Id: String = "id"
      val Position: String = "position"
      val X: String = "x"
      val Y: String = "y"
      val Orientation: String = "orientation"

      object StaticEntity:

        object Obstacle:
          val Self: String = "obstacle"
          val Width: String = "width"
          val Height: String = "height"

        object Light:
          val Self: String = "light"
          val Radius: String = "radius"
          val IlluminationRadius: String = "illuminationRadius"
          val Intensity: String = "intensity"
          val Attenuation: String = "attenuation"

      object DynamicEntity:

        object Robot:
          val Self: String = "robot"
          val Radius: String = "radius"
          val Speed: String = "speed"
          val WithProximitySensors: String = "withProximitySensors"
          val WithLightSensors: String = "withLightSensors"
          val Behavior: String = "behavior"

        object Agent:
          val Self: String = "agent"
          val Radius: String = "radius"
          val Speed: String = "speed"
          val WithProximitySensors: String = "withProximitySensors"
          val WithLightSensors: String = "withLightSensors"
          val Reward: String = "reward"
          val Termination: String = "termination"
          val Truncation: String = "truncation"

    end Entity
  end Fields

  object Layout:
    val SplitPaneWeight: Double = 0.8
    val SplitPaneLocation: Double = 0.8

  object Frame:
    val MinWidth = 800
    val MinHeight = 600
    val PrefWidth = 1200
    val PrefHeight = 720
    val SplitWeight = 0.8
    val CanvasBorder: Int = 2

  object Canvas:
    val BorderSize = 2
    val MinZoom = 0.2
    val MaxZoom = 5.0
    val ZoomInFactor = 1.2
    val ZoomOutFactor = 0.8
    val DesiredLabelPixels = 40.0
    val GridStrokeWidth = 1f
    val LabelDesiredPx: Double = 40.0
    val MinLightSize: Int = 12
    val LightStroke: Float = 2f
    val LabelBottomOffset: Int = 4
    val LabelYOffset: Int = 12
    val LabelXOffset: Int = 2

    val LabelStepSequence: List[Int] = List(1, 2, 5)
    val LabelScaleBase: Int = 10
    val DiameterFactor: Double = 2.0

    object Sensors:
      val LineStrokeWidth: Float = 1.0f
      val DotSize: Int = 3

    object LightFX:

      val GradientFraction0: Float = 0f
      val GradientFraction1: Float = 0.55f
      val GradientFraction2: Float = 0.9f
      val GradientFraction3: Float = 1f

      val CoreFractionStart: Float = 0f
      val CoreFractionEnd: Float = 1f

      val GradientRadiusDivisor: Float = 2f
      val MinLightPixels: Int = 1
      val PostGlowAlpha: Float = 0.015f

      val RingStrokeWidth: Float = 0.8f
      val RingStrokeMiterLimit: Float = 10f
      val RingStrokeDash1: Float = 4f
      val RingStrokeDash2: Float = 6f
      val RingStrokeDashPhase: Float = 0f

      val GradientColor: Array[Color] = Array(
        new Color(255, 200, 0, 28),
        new Color(255, 180, 0, 16),
        new Color(255, 160, 0, 6),
        new Color(255, 140, 0, 0),
      )

    end LightFX

    object Arrow:
      val TriangleVertices: Int = 3

    object RobotBody:
      val GradientFractionStart: Float = 0f
      val GradientFractionEnd: Float = 1f

  end Canvas

  object ControlsPanel:
    val StartStopButtonText: String = "Start/Stop"

end SimulationDefaults
