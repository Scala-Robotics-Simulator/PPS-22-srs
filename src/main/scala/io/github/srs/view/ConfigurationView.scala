package io.github.srs.view

import java.awt.{ BorderLayout, Dimension, FlowLayout }
import javax.swing.*

import cats.effect.IO
import io.github.srs.config.SimulationConfig
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.validation.DomainError
import io.github.srs.view.components.*
import io.github.srs.view.components.configuration.*
import io.github.srs.utils.SimulationDefaults.Fields.Entity
import io.github.srs.utils.SimulationDefaults.Fields.Entity.DynamicEntity.Robot as RobotFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Obstacle as ObstacleFields
import io.github.srs.utils.SimulationDefaults.Fields.Entity.StaticEntity.Light as LightFields
import io.github.srs.config.ConfigError
import io.github.srs.model.entity.dynamicentity.behavior.Policy

/**
 * Defines how the configuration view should behave.
 */
trait ConfigurationView:

  /**
   * Initializes the configuration view and displays it to the user.
   *
   * @return
   *   an IO effect that, when run, will return the simulation configuration chosen by the user.
   */
  def init(): IO[SimulationConfig]

  /**
   * Closes the configuration view.
   *
   * @return
   *   an IO effect that, when run, will close the view.
   */
  def close(): IO[Unit]

  /**
   * Companion object for ConfigurationView that provides a factory method to create an instance of ConfigurationView.
   */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.ToString",
  ),
)
object ConfigurationView:

  /**
   * Factory method to create an instance of ConfigurationView.
   *
   * @return
   *   a new instance of ConfigurationView.
   */
  def apply(): ConfigurationView = new ConfigurationViewImpl()

  private class ConfigurationViewImpl extends ConfigurationView:
    private val frame = new JFrame("Scala Robotics Simulator - Configuration")

    private val baseFieldSpec = Seq(
      FieldSpec(Entity.x, "X position", TextField(3)),
      FieldSpec(Entity.y, "Y position", TextField(3)),
      FieldSpec(Entity.orientation, "Orientation (degrees)", TextField(3)),
    )

    private val entityFieldSpecs: Map[String, Seq[FieldSpec]] = Map(
      RobotFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(RobotFields.radius, "Radius (meters)", TextField(2)),
        FieldSpec(RobotFields.speed, "Speed", TextField(3)),
        FieldSpec(RobotFields.withProximitySensors, "With proximity sensors", CheckBox(true)),
        FieldSpec(RobotFields.withLightSensors, "With light sensors", CheckBox(true)),
        FieldSpec(RobotFields.behavior, "Behavior", ComboBox(Policy.values.map(_.toString))),
      )),
      ObstacleFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(ObstacleFields.width, "Width", TextField(8)),
        FieldSpec(ObstacleFields.height, "Height", TextField(5)),
      )),
      LightFields.self.capitalize -> (baseFieldSpec ++ Seq(
        FieldSpec(LightFields.radius, "Radius (meters)", TextField(3)),
        FieldSpec(LightFields.illuminationRadius, "Illumination radius", TextField(3)),
        FieldSpec(LightFields.intensity, "Intensity", TextField(3)),
        FieldSpec(LightFields.attenuation, "Attenuation", TextField(3)),
      )),
    )

    // Component panels with callbacks
    private val simulationPanel = new SimulationSettingsPanel(
      onValidationError = showValidationErrors,
    )

    private val environmentPanel = new EnvironmentSettingsPanel(
      onValidationError = showValidationErrors,
    )

    private val entitiesPanel = new EntitiesPanel(entityFieldSpecs)

    private val controlsPanel = new ConfigurationControlsPanel(
      onConfigLoaded = loadConfiguration,
      onConfigSave = getCurrentConfiguration,
    )

    private val startButton = new JButton("Start Simulation")

    private def showValidationErrors(errors: Seq[String]): Unit =
      JOptionPane.showMessageDialog(
        frame,
        errors.mkString("\n"),
        "Validation Errors",
        JOptionPane.ERROR_MESSAGE,
      )

    private def loadConfiguration(config: SimulationConfig): Unit =
      // Update all panels with the loaded configuration
      simulationPanel.setSimulation(config.simulation)
      environmentPanel.setEnvironment(config.environment)
      entitiesPanel.setEntities(config.environment.entities)

    private def getCurrentConfiguration(): Option[SimulationConfig] =
      extractConfig()

    private def setupUI(): Unit =
      frame.setMinimumSize(new Dimension(700, 500))
      frame.setLayout(new BorderLayout())

      // Top panel with controls and settings
      val topPanel = new JPanel(new BorderLayout())
      topPanel.add(controlsPanel, BorderLayout.NORTH)

      val settingsPanel = new JPanel(new BorderLayout())
      settingsPanel.add(simulationPanel, BorderLayout.NORTH)
      settingsPanel.add(environmentPanel, BorderLayout.SOUTH)
      topPanel.add(settingsPanel, BorderLayout.SOUTH)

      // Bottom panel with start button
      val bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT))
      bottomPanel.add(startButton)

      frame.add(topPanel, BorderLayout.NORTH)
      frame.add(entitiesPanel, BorderLayout.CENTER)
      frame.add(bottomPanel, BorderLayout.SOUTH)

      frame.pack()
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    end setupUI

    def init(): IO[SimulationConfig] =
      setupUI()
      frame.setVisible(true)
      IO.async_(cb =>
        startButton.addActionListener: _ =>
          extractConfig() match
            case Some(config) => cb(Right[Throwable, SimulationConfig](config))
            case None =>
              showValidationErrors(Seq("Please fix configuration errors before starting")),
      )

    private def extractConfig(): Option[SimulationConfig] =
      val simulationResult = simulationPanel.getSimulation
      val environmentResult = environmentPanel.getEnvironmentBase
      val entitiesResult = entitiesPanel.getEntities

      (simulationResult, environmentResult, entitiesResult) match
        case (Right(simulation), Right(environmentBase), Right(entities)) =>
          val environment = environmentBase.copy(entities = entities.toSet)
          environment.validate match
            case Left(error) =>
              showValidationErrors(Seq(s"Environment validation error: ${error.errorMessage}"))
              None
            case Right(validatedEnv) =>
              Some(SimulationConfig(simulation, validatedEnv))
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
      end match
    end extractConfig

    override def close(): IO[Unit] = IO.pure(frame.dispose())

  end ConfigurationViewImpl

end ConfigurationView
