package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.*

import io.github.srs.view.components.FieldSpec

@SuppressWarnings(
  Array(
    "org.wartremover.warts.AsInstanceOf",
    "scalafix:DisableSyntax.asInstanceOf",
  ),
)
class EntitiesPanel(fieldSpecsByType: Map[String, Seq[FieldSpec]]) extends JPanel(new BorderLayout):
  private val entityListPanel = new JPanel()
  private val btnAddEntity = new JButton("+")

  initPanel()

  private def initPanel(): Unit =
    setBorder(BorderFactory.createTitledBorder("Entities"))
    val topBar = new JPanel(new FlowLayout(FlowLayout.LEFT))
    topBar.add(new JLabel("Add or remove entities:"))
    btnAddEntity.setToolTipText("Add a new entity")
    topBar.add(btnAddEntity)
    add(topBar, BorderLayout.NORTH)

    entityListPanel.setLayout(new BoxLayout(entityListPanel, BoxLayout.Y_AXIS))
    val scroll = new JScrollPane(entityListPanel)
    scroll.setBorder(BorderFactory.createEmptyBorder())
    add(scroll, BorderLayout.CENTER)

    btnAddEntity.addActionListener(_ =>
      val types = fieldSpecsByType.keys.toArray
      val selector = new JComboBox[String](types)
      val result = JOptionPane.showConfirmDialog(
        this,
        selector,
        "Select entity type",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE,
      )
      if result == JOptionPane.OK_OPTION then
        val selected = selector.getSelectedItem.asInstanceOf[String]
        val row = new EntityRow(selected, fieldSpecsByType, removeEntityRow)
        entityListPanel.add(row)
        entityListPanel.revalidate()
        entityListPanel.repaint(),
    )

  end initPanel

  private def removeEntityRow(row: JPanel): Unit =
    entityListPanel.remove(row)
    entityListPanel.revalidate()
    entityListPanel.repaint()

  def getEntities: Seq[(String, Map[String, Any])] =
    entityListPanel.getComponents.collect { case r: EntityRow =>
      r.getEntityType -> r.getEntityValues
    }
end EntitiesPanel
