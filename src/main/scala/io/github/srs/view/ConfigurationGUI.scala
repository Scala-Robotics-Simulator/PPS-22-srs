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
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Actuator.DifferentialWheelMotor.Wheel.defaultSpeed
import io.github.srs.utils.SimulationDefaults.StaticEntity.Obstacle.{ defaultHeight, defaultWidth }
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
      FieldSpec(Entity.x, "X position", TextField(3, default = defaultPosition.x.toString)),
      FieldSpec(Entity.y, "Y position", TextField(3, default = defaultPosition.y.toString)),
      FieldSpec(
        Entity.orientation,
        "Orientation (degrees)",
        TextField(3, default = defaultOrientation.degrees.toString),
      ),
    )

    private val entityFieldSpecs: Map[String, Seq[FieldSpec]] = Map(
      RobotFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(RobotFields.radius, "Radius (meters)", TextField(2, default = defaultShape.radius.toString)),
        FieldSpec(RobotFields.speed, "Speed", TextField(3, default = defaultSpeed.toString)),
        FieldSpec(RobotFields.withProximitySensors, "With proximity sensors", CheckBox(true)),
        FieldSpec(RobotFields.withLightSensors, "With light sensors", CheckBox(true)),
        FieldSpec(RobotFields.behavior, "Behavior", ComboBox(Policy.values.toSeq.map(_.toString))),
      )),
      ObstacleFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(ObstacleFields.width, "Width", TextField(8, default = defaultWidth.toString)),
        FieldSpec(ObstacleFields.height, "Height", TextField(5, default = defaultHeight.toString)),
      )),
      LightFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(LightFields.radius, "Radius (meters)", TextField(3, default = Light.defaultRadius.toString)),
        FieldSpec(
          LightFields.illuminationRadius,
          "Illumination radius",
          TextField(3, default = Light.defaultIlluminationRadius.toString),
        ),
        FieldSpec(LightFields.intensity, "Intensity", TextField(3, Light.defaultIntensity.toString)),
        FieldSpec(LightFields.attenuation, "Attenuation", TextField(3, Light.defaultAttenuation.toString)),
      )),
    )

    // Component panels with callbacks
    private val simulationPanel = new SimulationSettingsPanel(
      onValidationError = showValidationErrors,
    )

    private val environmentPanel = new EnvironmentSettingsPanel(
      onValidationError = showValidationErrors,
    )

    private val fieldCanvas = new SimulationCanvas(alwaysRefresh = true)

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
