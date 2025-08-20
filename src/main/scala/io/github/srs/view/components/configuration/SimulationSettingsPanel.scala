package io.github.srs.view.components.configuration

import java.awt.BorderLayout
import javax.swing.*

import io.github.srs.view.components.{ FieldSpec, FormPanel }
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.model.Simulation
import io.github.srs.model.Simulation.*
import io.github.srs.utils.chaining.Pipe.given
import io.github.srs.utils.SimulationDefaults.Fields.Simulation as SimulationFields
import io.github.srs.config.ConfigError

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
    FieldSpec(SimulationFields.duration, "Duration", io.github.srs.view.components.TextField(10)),
    FieldSpec(SimulationFields.seed, "Seed", io.github.srs.view.components.TextField(10)),
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
      duration <- getOptional[Long](SimulationFields.duration, fieldValues)
      seed <- getOptional[Long](SimulationFields.seed, fieldValues)
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
      SimulationFields.duration -> sim.duration.map(_.toString).getOrElse(""),
      SimulationFields.seed -> sim.seed.map(_.toString).getOrElse(""),
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
          case ConfigError.MissingField(field) => s"Missing field: $field"
          case ConfigError.ParsingError(message) => s"Parsing error: $message"
          case ConfigError.InvalidType(field, expected) =>
            s"Invalid type for $field: expected $expected"
        onValidationError(errorMessages)
        false
      case Right(_) => true

end SimulationSettingsPanel
