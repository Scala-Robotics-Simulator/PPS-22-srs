package io.github.srs.view

import javax.swing.*
import java.awt.{ BorderLayout, Dimension, FlowLayout }

import cats.effect.IO
import io.github.srs.view.components.configuration.EntitiesPanel
import io.github.srs.view.components.*
import io.github.srs.config.SimulationConfig
import io.github.srs.model.validation.DomainError
import io.github.srs.model.Simulation.simulation
import io.github.srs.model.Simulation
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*

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

    private val simulationFields = Seq(
      FieldSpec("duration", "Duration", TextField(10)),
      FieldSpec("seed", "Seed", TextField(10)),
    )

    private val environmentFields = Seq(
      FieldSpec("width", "Width", TextField(5)),
      FieldSpec("height", "Height", TextField(5)),
    )

    private val baseFieldSpec = Seq(
      FieldSpec("x position", "X", TextField(3)),
      FieldSpec("y position", "Y", TextField(3)),
    )

    private val baseAndOrient = baseFieldSpec :+ FieldSpec("orientation degrees", "Orientation (degrees)", TextField(3))

    private val entityFieldSpecs: Map[String, Seq[FieldSpec]] = Map(
      "Robot" -> (baseAndOrient ++ Seq(
        FieldSpec("radius meters", "Radius (meters)", TextField(2)),
        FieldSpec("speed", "Speed", TextField(3)),
        FieldSpec("containing proximity sensors", "With proximity sensors", CheckBox(true)),
        FieldSpec("containing light sensors", "With light sensors", CheckBox(true)),
      )),
      "Obstacle" -> (baseAndOrient ++ Seq(
        FieldSpec("width", "Width", TextField(8)),
        FieldSpec("height", "Height", TextField(5)),
      )),
      "Light" -> (baseFieldSpec ++ Seq(
        FieldSpec("illumination radius", "Illumination radius", TextField(3)),
        FieldSpec("intensity", "Intensity", TextField(3)),
        FieldSpec("attenuation", "Attenuation", TextField(3)),
      )),
    )

    private val simulationPanel = new FormPanel("Simulation Settings", simulationFields)
    private val environmentPanel = new FormPanel("Environment Settings", environmentFields)
    private val entitiesPanel = new EntitiesPanel(entityFieldSpecs)

    private val loadButton = new JButton("Load")
    private val saveButton = new JButton("Save")
    private val startButton = new JButton("Start")

    def init(): IO[SimulationConfig] =
      frame.setMinimumSize(new Dimension(700, 500))
      frame.setLayout(new BorderLayout())

      // Top panel with buttons and settings
      val topPanel = new JPanel(new BorderLayout())

      val buttonPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT))
      buttonPanelTop.add(loadButton)
      buttonPanelTop.add(saveButton)

      val settingsPanel = new JPanel(new BorderLayout())
      settingsPanel.add(simulationPanel, BorderLayout.NORTH)
      settingsPanel.add(environmentPanel, BorderLayout.SOUTH)

      topPanel.add(buttonPanelTop, BorderLayout.NORTH)
      topPanel.add(settingsPanel, BorderLayout.SOUTH)

      // Bottom panel with start button
      val bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT))
      bottomPanel.add(startButton)

      frame.add(topPanel, BorderLayout.NORTH)
      frame.add(entitiesPanel, BorderLayout.CENTER)
      frame.add(bottomPanel, BorderLayout.SOUTH)

      frame.pack()
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.setVisible(true)

      IO.async_[SimulationConfig] { cb =>
        // set up GUI and event listener
        startButton.addActionListener { _ =>
          loadConfig() match
            case Left(error) => JOptionPane.showMessageDialog(frame, error.errorMessage);
            case Right(cfg) => cb(Right[Throwable, SimulationConfig](cfg))
        }
      }
    end init

    override def close(): IO[Unit] = IO.pure(frame.dispose())

    private def loadConfig(): Either[DomainError, SimulationConfig] =
      // TODO: update creating the correct simulation config
      val simulation = loadSimulation()
      val environment = loadEnvironment()
      for env <- environment.validate
      yield SimulationConfig(simulation, env)

    private def loadSimulation(): Simulation =
      simulation

    private def loadEnvironment(): Environment =
      environment

    def getSimulationSettings: Map[String, Any] = simulationPanel.getValues
    def getEnvironmentSettings: Map[String, Any] = environmentPanel.getValues
    def getEntities: Seq[(String, Map[String, Any])] = entitiesPanel.getEntities
  end ConfigurationViewImpl
end ConfigurationView
