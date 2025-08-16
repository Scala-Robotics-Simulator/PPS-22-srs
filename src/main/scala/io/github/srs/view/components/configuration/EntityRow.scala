package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.*

import io.github.srs.view.components.FieldSpec
import io.github.srs.view.components.FormPanel

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

  def getEntityType: String = typeCombo.getSelectedItem.asInstanceOf[String]
  def getEntityValues: Map[String, Any] = propertiesPanel.getValues
end EntityRow
