package io.github.srs.view.components

import java.awt.*
import javax.swing.*

/**
 * Represents the type of input fields in the form.
 */
sealed trait InputType
final case class TextField(columns: Int = 10) extends InputType
final case class ComboBox(options: Seq[String]) extends InputType
final case class CheckBox(default: Boolean = false) extends InputType

final case class FieldSpec(key: String, label: String, inputType: InputType)

/**
 * FormPanel is a JPanel that displays a form with various input fields.
 * @param title
 *   the title of the form panel
 * @param fields
 *   the specifications of the fields to be displayed in the form
 */
class FormPanel(title: String, fields: Seq[FieldSpec]) extends JPanel(new GridBagLayout):

  private val inputs: Map[String, JComponent] = fields.map { spec =>
    val comp: JComponent = spec.inputType match
      case TextField(cols) => new JTextField(cols)
      case ComboBox(opts) => new JComboBox[String](opts.toArray)
      case CheckBox(defaultVal) => new JCheckBox("", defaultVal)
    spec.key -> comp
  }.toMap

  initUI()

  private def initUI(): Unit =
    setBorder(BorderFactory.createTitledBorder(title))

    val gbc = new GridBagConstraints()
    gbc.insets = new Insets(4, 8, 4, 8)
    gbc.fill = GridBagConstraints.HORIZONTAL

    fields.zipWithIndex.foreach { case (spec, row) =>
      gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0
      add(new JLabel(spec.label + ":"), gbc)

      gbc.gridx = 1; gbc.weightx = 1.0
      add(inputs(spec.key), gbc)
    }

  /**
   * Retrieves the values from the form inputs as a Map.
   *
   * @return
   *   a Map where keys are field names and values are the corresponding input values
   */
  def getValues: Map[String, Any] =
    inputs.map:
      case (key, tf: JTextField) => key -> tf.getText
      case (key, cb: JComboBox[?]) => key -> cb.getSelectedItem
      case (key, chk: JCheckBox) => key -> chk.isSelected
      case (key, comp: JComponent) =>
        println(s"got for key $key component ${comp.toString()}, unable to get the value, exiting...")
        sys.exit(1)
end FormPanel
