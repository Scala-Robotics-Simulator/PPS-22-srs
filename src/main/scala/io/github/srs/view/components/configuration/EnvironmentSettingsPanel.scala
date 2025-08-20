package io.github.srs.view.components.configuration

import java.awt.BorderLayout
import javax.swing.*

import io.github.srs.view.components.{ FieldSpec, FormPanel }
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.Decoder
import io.github.srs.model.environment.Environment
import io.github.srs.model.environment.dsl.CreationDSL.*

/**
 * EnvironmentSettingsPanel handles the environment-specific configuration settings. It provides a form for configuring
 * environment width and height.
 *
 * @param onValidationError
 *   callback triggered when validation fails
 */
class EnvironmentSettingsPanel(
    onValidationError: Seq[String] => Unit,
) extends JPanel(new BorderLayout):

  private val environmentFields = Seq(
    FieldSpec("width", "Width", io.github.srs.view.components.TextField(5)),
    FieldSpec("height", "Height", io.github.srs.view.components.TextField(5)),
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
      width <- get[Int]("width", fieldValues)
      height <- get[Int]("height", fieldValues)
    yield environment withWidth width withHeight height

  /**
   * Sets the environment configuration values in the form fields.
   *
   * @param env
   *   the environment configuration to display
   */
  def setEnvironment(env: Environment): Unit =
    val environmentMap = Map(
      "width" -> env.width.toString,
      "height" -> env.height.toString,
    )
    environmentPanel.setValues(environmentMap)

  /**
   * Validates the current form values and returns whether they are valid.
   *
   * @return
   *   true if values are valid, false otherwise
   */
  def validateFields(): Boolean =
    getEnvironmentBase match
      case Left(errors) =>
        val errorMessages = errors.map:
          case io.github.srs.config.ConfigError.MissingField(field) => s"Missing field: $field"
          case io.github.srs.config.ConfigError.ParsingError(message) => s"Parsing error: $message"
          case io.github.srs.config.ConfigError.InvalidType(field, expected) =>
            s"Invalid type for $field: expected $expected"
        onValidationError(errorMessages)
        false
      case Right(_) => true

end EnvironmentSettingsPanel
