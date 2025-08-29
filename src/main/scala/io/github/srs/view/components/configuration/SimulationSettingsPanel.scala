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

/**
 * SimulationSettingsPanel handles the simulation-specific configuration settings. It provides a form for configuring
 * simulation duration and seed.
 */
class SimulationSettingsPanel extends JPanel(new BorderLayout):

  private val simulationFields = Seq(
    FieldSpec(SimulationFields.Duration, "Duration", io.github.srs.view.components.TextField()),
    FieldSpec(SimulationFields.Seed, "Seed", io.github.srs.view.components.TextField()),
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
      duration <- getOptional[Long](SimulationFields.Duration, fieldValues)
      seed <- getOptional[Long](SimulationFields.Seed, fieldValues)
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
      SimulationFields.Duration -> sim.duration.map(_.toString).getOrElse(""),
      SimulationFields.Seed -> sim.seed.map(_.toString).getOrElse(""),
    )
    simulationPanel.setValues(simulationMap)
end SimulationSettingsPanel
