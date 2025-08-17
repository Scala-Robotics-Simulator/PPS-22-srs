package io.github.srs.view

import java.awt.{ BorderLayout, Dimension, FlowLayout }
import javax.swing.*

import cats.effect.IO
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.config.{ ConfigResult, SimulationConfig }
import io.github.srs.model.Simulation
import io.github.srs.model.Simulation.*
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.model.validation.DomainError
import io.github.srs.utils.chaining.Pipe.given
import io.github.srs.view.components.*
import io.github.srs.view.components.configuration.EntitiesPanel

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
      FieldSpec("duration", "Duration (duration)", TextField(10)),
      FieldSpec("seed", "Seed (seed)", TextField(10)),
    )

    private val environmentFields = Seq(
      FieldSpec("width", "Width (width)", TextField(5)),
      FieldSpec("height", "Height (height)", TextField(5)),
    )

    private val baseFieldSpec = Seq(
      FieldSpec("x", "X position (x)", TextField(3)),
      FieldSpec("y", "Y position (y)", TextField(3)),
    )

    private val baseAndOrient =
      baseFieldSpec :+ FieldSpec("orientation", "Orientation (degrees) (orientation)", TextField(3))

    private val entityFieldSpecs: Map[String, Seq[FieldSpec]] = Map(
      "Robot" -> (baseAndOrient ++ Seq(
        FieldSpec("radius", "Radius (meters) (radius)", TextField(2)),
        FieldSpec("speed", "Speed (speed)", TextField(3)),
        FieldSpec("proxSens", "With proximity sensors (proxSens)", CheckBox(true)),
        FieldSpec("lightSens", "With light sensors (lightSens)", CheckBox(true)),
      )),
      "Obstacle" -> (baseAndOrient ++ Seq(
        FieldSpec("width", "Width (width)", TextField(8)),
        FieldSpec("height", "Height (height)", TextField(5)),
      )),
      "Light" -> (baseFieldSpec ++ Seq(
        FieldSpec("illumination", "Illumination radius (illumination)", TextField(3)),
        FieldSpec("intensity", "Intensity (intensity)", TextField(3)),
        FieldSpec("attenuation", "Attenuation (attenuation)", TextField(3)),
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
          loadSimulation() match
            case Left(errors) => JOptionPane.showMessageDialog(frame, errors.fold("")((acc, err) => s"$acc$err \n"))
            case Right(sim) =>
              loadEnvironment() match
                case Left(errors) => JOptionPane.showMessageDialog(frame, errors.fold("")((acc, err) => s"$acc$err \n"))
                case Right(env) =>
                  loadConfig(sim, env) match
                    case Left(error) => JOptionPane.showMessageDialog(frame, error.errorMessage);
                    case Right(cfg) => cb(Right[Throwable, SimulationConfig](cfg))
        }
      }
    end init

    override def close(): IO[Unit] = IO.pure(frame.dispose())

    private def loadConfig(
        simulation: Simulation,
        environment: Environment,
    ): Either[DomainError, SimulationConfig] =
      // TODO: update creating the correct simulation config
      for env <- environment.validate
      yield SimulationConfig(simulation, env)

    private def loadSimulation(): ConfigResult[Simulation] =
      import Decoder.{ getOptional, given }
      val map = simulationPanel.getValues
      for
        duration <- getOptional[Long]("duration", map)
        seed <- getOptional[Long]("seed", map)
      yield simulation
        |> (s => duration.fold(s)(s.withDuration))
        |> (s => seed.fold(s)(s.withSeed))

    private def loadEnvironment(): ConfigResult[Environment] =
      import Decoder.{ get, given }
      val map = environmentPanel.getValues
      for
        width <- get[Int]("width", map)
        height <- get[Int]("height", map)
        entities <- entitiesPanel.getEntities
      yield environment withWidth width withHeight height containing entities.toSet

  end ConfigurationViewImpl
end ConfigurationView
