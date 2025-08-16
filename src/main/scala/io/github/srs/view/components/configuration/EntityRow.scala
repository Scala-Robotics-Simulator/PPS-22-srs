package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.*

import io.github.srs.view.components.FieldSpec
import io.github.srs.view.components.FormPanel

/**
 * EntityRow is a JPanel that represents a single entity in the configuration view. It allows users to select the type
 * of entity and fill in its properties.
 *
 * @param initialType
 *   the initial type of the entity
 * @param fieldSpecsByType
 *   a map where keys are entity types and values are sequences of FieldSpec defining the fields for each entity type
 * @param removeRow
 *   a function to call when the user wants to remove this row from the configuration
 */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "scalafix:DisableSyntax.asInstanceOf",
    "org.wartremover.warts.Var",
  ),
)
class EntityRow(
    initialType: String,
    fieldSpecsByType: Map[String, Seq[FieldSpec]],
    removeRow: JPanel => Unit,
) extends JPanel(new BorderLayout):

  private val typeCombo = new JComboBox[String](fieldSpecsByType.keys.toArray)
  typeCombo.setSelectedItem(initialType)

  private var propertiesPanel: FormPanel = new FormPanel("", fieldSpecsByType(initialType))

  private val btnRemove = new JButton("X")
  btnRemove.setToolTipText("Remove this entity")

  private val rowPanel = new JPanel(new GridBagLayout)
  private val gbc = new GridBagConstraints()
  gbc.insets = new Insets(4, 4, 4, 4)
  gbc.fill = GridBagConstraints.HORIZONTAL

  gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0
  rowPanel.add(typeCombo, gbc)

  gbc.gridx = 1; gbc.weightx = 1.0
  rowPanel.add(propertiesPanel, gbc)

  gbc.gridx = 2; gbc.weightx = 0
  rowPanel.add(btnRemove, gbc)

  add(rowPanel, BorderLayout.CENTER)
  add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH)

  typeCombo.addActionListener(_ =>
    val selected = typeCombo.getSelectedItem.asInstanceOf[String]
    rowPanel.remove(propertiesPanel)
    propertiesPanel = new FormPanel("", fieldSpecsByType(selected))
    gbc.gridx = 1; gbc.weightx = 1.0
    rowPanel.add(propertiesPanel, gbc)
    revalidate()
    repaint(),
  )

  btnRemove.addActionListener(_ => removeRow(this))

  /**
   * Retrieves the type and values of the entity represented by this row.
   *
   * @return
   *   a tuple containing the entity type and a map of its properties
   */
  def getEntityType: String = typeCombo.getSelectedItem.asInstanceOf[String]

  /**
   * Retrieves the values of the properties for the entity represented by this row.
   *
   * @return
   *   a map where keys are property names and values are the corresponding input values
   */
  def getEntityValues: Map[String, Any] = propertiesPanel.getValues
end EntityRow
