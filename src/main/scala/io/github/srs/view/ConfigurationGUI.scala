package io.github.srs.view

import java.awt.{ BorderLayout, FlowLayout }
import javax.swing.*

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.srs.config.{ ConfigError, SimulationConfig }
import io.github.srs.model.entity.dynamicentity.behavior.Policy
import io.github.srs.model.environment.{ Environment, ValidEnvironment }
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.validation.DomainError
import io.github.srs.utils.SimulationDefaults.Fields.Entity
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.{
  Light as LightFields,
  Obstacle as ObstacleFields,
}
import io.github.srs.view.components.*
import io.github.srs.view.components.configuration.*
import io.github.srs.view.components.simulation.SimulationCanvas
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.*
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor.Wheel.DefaultSpeed
import io.github.srs.utils.SimulationDefaults.StaticEntity.Obstacle.{ DefaultHeight, DefaultWidth }
import io.github.srs.utils.SimulationDefaults.StaticEntity.Light

/**
 * ConfigurationView companion object with factory method to create an instance of a GUI-based configuration view.
 */
object ConfigurationGUI:

  /**
   * Factory method to create an instance of ConfigurationView.
   *
   * @return
   *   a new instance of ConfigurationView.
   */
  def apply(): ConfigurationView = new ConfigurationViewImpl()

  private class ConfigurationViewImpl extends ConfigurationView:
    import io.github.srs.model.entity.Point2D.*
    private val frame = new JFrame("Scala Robotics Simulator - Configuration")

    private val baseFieldSpec = Seq(
      FieldSpec(Entity.X, "X position", TextField(3, default = DefaultPosition.x.toString)),
      FieldSpec(Entity.Y, "Y position", TextField(3, default = DefaultPosition.y.toString)),
      FieldSpec(
        Entity.Orientation,
        "Orientation (degrees)",
        TextField(3, default = DefaultOrientation.degrees.toString),
      ),
    )

    private val entityFieldSpecs: Map[String, Seq[FieldSpec]] = Map(
      RobotFields.Self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(RobotFields.Radius, "Radius (meters)", TextField(2, default = DefaultShape.radius.toString)),
        FieldSpec(RobotFields.Speed, "Speed", TextField(3, default = DefaultSpeed.toString)),
        FieldSpec(RobotFields.WithProximitySensors, "With proximity sensors", CheckBox(true)),
        FieldSpec(RobotFields.WithLightSensors, "With light sensors", CheckBox(true)),
        FieldSpec(RobotFields.Behavior, "Behavior", ComboBox(Policy.values.toSeq.map(_.toString))),
      )),
      ObstacleFields.Self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(ObstacleFields.Width, "Width", TextField(8, default = DefaultWidth.toString)),
        FieldSpec(ObstacleFields.Height, "Height", TextField(5, default = DefaultHeight.toString)),
      )),
      LightFields.Self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(LightFields.Radius, "Radius (meters)", TextField(3, default = Light.DefaultRadius.toString)),
        FieldSpec(
          LightFields.IlluminationRadius,
          "Illumination radius",
          TextField(3, default = Light.DefaultIlluminationRadius.toString),
        ),
        FieldSpec(LightFields.Intensity, "Intensity", TextField(3, Light.DefaultIntensity.toString)),
        FieldSpec(LightFields.Attenuation, "Attenuation", TextField(3, Light.DefaultAttenuation.toString)),
      )),
    )

    // Component panels with callbacks
    private val simulationPanel = new SimulationSettingsPanel

    private val environmentPanel = new EnvironmentSettingsPanel

    private val fieldCanvas = new SimulationCanvas(insideConfiguration = true)

    private def refreshCanvas(env: Environment): IO[Unit] =
      IO:
        fieldCanvas.update(env = env, selectedId = None)

    private val entitiesPanel = new EntitiesPanel(entityFieldSpecs)

    private val controlsPanel = new ConfigurationControlsPanel(
      onConfigLoaded = loadConfiguration,
      onConfigSave = extractConfig,
    )

    private val refreshFieldButton = new JButton("Refresh Field")
    private val startButton = new JButton("Start Simulation")

    private def showValidationErrors(errors: Seq[String]): Unit =
      JOptionPane.showMessageDialog(
        frame,
        errors.mkString("\n"),
        "Validation Errors",
        JOptionPane.ERROR_MESSAGE,
      )

    private def loadConfiguration(config: SimulationConfig[Environment]): Unit =
      // Update all panels with the loaded configuration
      simulationPanel.setSimulation(config.simulation)
      environmentPanel.setEnvironment(config.environment)
      entitiesPanel.setEntities(config.environment.entities)
      refreshCanvas(config.environment).unsafeRunAsync(_ => ())

    private def setupUI(splitRatio: Double = 0.5): Unit =
      frame.applyDefaultAndPreferSize()
      frame.setLayout(new BorderLayout())
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

      // Top panel with controls and settings
      val topPanel = new JPanel(new BorderLayout())
      topPanel.add(controlsPanel, BorderLayout.NORTH)

      val settingsPanel = new JPanel(new BorderLayout())
      settingsPanel.add(simulationPanel, BorderLayout.NORTH)
      settingsPanel.add(environmentPanel, BorderLayout.SOUTH)
      topPanel.add(settingsPanel, BorderLayout.SOUTH)

      // Bottom panel with start/refresh buttons
      val bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT))
      bottomPanel.add(refreshFieldButton)
      bottomPanel.add(startButton)

      // Left side (settings + entities)
      val leftPanel = new JPanel(new BorderLayout())
      leftPanel.add(topPanel, BorderLayout.NORTH)
      leftPanel.add(entitiesPanel, BorderLayout.CENTER)

      // Split pane: left panels vs simulation canvas
      val splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, fieldCanvas)
      splitPane.setResizeWeight(splitRatio)
      splitPane.setContinuousLayout(true)

      frame.add(splitPane, BorderLayout.CENTER)
      frame.add(bottomPanel, BorderLayout.SOUTH)

      frame.pack()
      frame.centerFrame()

      refreshFieldButton.addActionListener: _ =>
        val env = environmentPanel.getEnvironmentBase
        val entities = entitiesPanel.getEntities
        (env, entities) match
          case (Right(env), Right(entities)) =>
            refreshCanvas(env.copy(entities = entities.toSet)).unsafeRunAsync(_ => ())
          case _ => ()

    end setupUI

    def init(): IO[SimulationConfig[ValidEnvironment]] =
      setupUI()
      frame.setVisible(true)
      IO.async_(cb =>
        startButton.addActionListener: _ =>
          extractConfig() match
            case Some(config) =>
              config.environment.validate match
                case Left(error) =>
                  showValidationErrors(Seq(s"Environment validation error: ${error.errorMessage}"))
                case Right(validatedEnv) =>
                  cb(
                    Right[Throwable, SimulationConfig[ValidEnvironment]](
                      SimulationConfig[ValidEnvironment](config.simulation, validatedEnv),
                    ),
                  )

            case None =>
              showValidationErrors(Seq("Please fix configuration errors before starting")),
      )

    end init

    private def extractConfig(): Option[SimulationConfig[Environment]] =
      val simulationResult = simulationPanel.getSimulation
      val environmentResult = environmentPanel.getEnvironmentBase
      val entitiesResult = entitiesPanel.getEntities

      (simulationResult, environmentResult, entitiesResult) match
        case (Right(simulation), Right(environmentBase), Right(entities)) =>
          val environment = environmentBase.copy(entities = entities.toSet)
          Some(SimulationConfig(simulation, environment))
        case _ =>
          val allErrors = Seq(simulationResult, environmentResult, entitiesResult).collect { case Left(errors) =>
            errors
          }.flatten
            .map:
              case ConfigError.MissingField(field) => s"Missing field: $field"
              case ConfigError.ParsingError(message) => s"Parsing error: $message"
              case ConfigError.InvalidType(field, expected) =>
                s"Invalid type for $field: expected $expected"
          showValidationErrors(allErrors)
          None
    end extractConfig

    override def close(): IO[Unit] = IO.pure(frame.dispose())

  end ConfigurationViewImpl

end ConfigurationGUI
