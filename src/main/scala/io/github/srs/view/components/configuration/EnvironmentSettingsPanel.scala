package io.github.srs.view.components.configuration

import java.awt.BorderLayout
import javax.swing.*

import io.github.srs.view.components.{ FieldSpec, FormPanel }
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*
import io.github.srs.utils.SimulationDefaults.Fields.Environment as EnvironmentFields

/**
 * EnvironmentSettingsPanel handles the environment-specific configuration settings. It provides a form for configuring
 * environment width and height.
 *
 * @param onValidationError
 *   callback triggered when validation fails
 */
class EnvironmentSettingsPanel extends JPanel(new BorderLayout):

  private val environmentFields = Seq(
    FieldSpec(EnvironmentFields.Width, "Width", io.github.srs.view.components.TextField(5)),
    FieldSpec(EnvironmentFields.Height, "Height", io.github.srs.view.components.TextField(5)),
  )

  private val environmentPanel = new FormPanel("Environment Settings", environmentFields)

  initPanel()

  private def initPanel(): Unit =
    add(environmentPanel, BorderLayout.CENTER)

  /**
   * Extracts the environment configuration from the form fields. Note: This returns the base environment without
   * entities, which should be added separately.
   *
   * @return
   *   either validation errors or a valid Environment (without entities)
   */
  def getEnvironmentBase: ConfigResult[Environment] =
    import Decoder.{ get, given }
    val fieldValues = environmentPanel.getValues

    for
      width <- get[Int](EnvironmentFields.Width, fieldValues)
      height <- get[Int](EnvironmentFields.Height, fieldValues)
    yield environment withWidth width withHeight height

  /**
   * Sets the environment configuration values in the form fields.
   *
   * @param env
   *   the environment configuration to display
   */
  def setEnvironment(env: Environment): Unit =
    val environmentMap = Map(
      EnvironmentFields.Width -> env.width.toString,
      EnvironmentFields.Height -> env.height.toString,
    )
    environmentPanel.setValues(environmentMap)
end EnvironmentSettingsPanel
