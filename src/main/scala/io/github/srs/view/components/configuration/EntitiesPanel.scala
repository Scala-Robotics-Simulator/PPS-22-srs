package io.github.srs.view.components.configuration

import javax.swing.*
import java.awt.*

import io.github.srs.view.components.FieldSpec
import io.github.srs.model.entity.Entity
import io.github.srs.config.ConfigResult
import io.github.srs.config.yaml.parser.collection.CustomSeq.sequence

/**
 * EntitiesPanel is a JPanel that allows users to add and remove the simulation entities.
 *
 * @param fieldSpecsByType
 *   a map where keys are entity types and values are sequences of FieldSpec defining the fields for each entity type
 */
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

    /**
     * Retrieves the list of entities currently in the panel.
     *
     * @return
     *   a sequence of tuples where each tuple contains the entity type and a map of its values
     */
  def getEntities: ConfigResult[Seq[Entity]] =
    entityListPanel.getComponents.collect { case r: EntityRow =>
      r.getEntity
    }.sequence
end EntitiesPanel
