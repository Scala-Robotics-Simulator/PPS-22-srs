package io.github.srs.view.components.configuration

import java.awt.BorderLayout
import javax.swing.*

import io.github.srs.view.components.{ FieldSpec, FormPanel }
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.model.Simulation
import io.github.srs.model.Simulation.*
import io.github.srs.utils.chaining.Pipe.given

/**
 * SimulationSettingsPanel handles the simulation-specific configuration settings. It provides a form for configuring
 * simulation duration and seed.
 *
 * @param onValidationError
 *   callback triggered when validation fails
 */
class SimulationSettingsPanel(
    onValidationError: Seq[String] => Unit,
) extends JPanel(new BorderLayout):

  private val simulationFields = Seq(
    FieldSpec("duration", "Duration", io.github.srs.view.components.TextField(10)),
    FieldSpec("seed", "Seed", io.github.srs.view.components.TextField(10)),
  )

  private val simulationPanel = new FormPanel("Simulation Settings", simulationFields)

  initPanel()

  private def initPanel(): Unit =
    add(simulationPanel, BorderLayout.CENTER)

  /**
   * Extracts the simulation configuration from the form fields.
   *
   * @return
   *   either validation errors or a valid Simulation
   */
  def getSimulation: ConfigResult[Simulation] =
    import Decoder.{ getOptional, given }
    val fieldValues = simulationPanel.getValues

    for
      duration <- getOptional[Long]("duration", fieldValues)
      seed <- getOptional[Long]("seed", fieldValues)
    yield simulation
      |> (s => duration.fold(s)(s.withDuration))
      |> (s => seed.fold(s)(s.withSeed))

  /**
   * Sets the simulation configuration values in the form fields.
   *
   * @param sim
   *   the simulation configuration to display
   */
  def setSimulation(sim: Simulation): Unit =
    val simulationMap = Map(
      "duration" -> sim.duration.map(_.toString).getOrElse(""),
      "seed" -> sim.seed.map(_.toString).getOrElse(""),
    )
    simulationPanel.setValues(simulationMap)

  /**
   * Validates the current form values and returns whether they are valid.
   *
   * @return
   *   true if values are valid, false otherwise
   */
  def validateFields(): Boolean =
    getSimulation match
      case Left(errors) =>
        val errorMessages = errors.map:
          case io.github.srs.config.ConfigError.MissingField(field) => s"Missing field: $field"
          case io.github.srs.config.ConfigError.ParsingError(message) => s"Parsing error: $message"
          case io.github.srs.config.ConfigError.InvalidType(field, expected) =>
            s"Invalid type for $field: expected $expected"
        onValidationError(errorMessages)
        false
      case Right(_) => true

end SimulationSettingsPanel
