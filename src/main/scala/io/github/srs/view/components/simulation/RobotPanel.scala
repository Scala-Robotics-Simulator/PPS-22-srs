package io.github.srs.view.components.simulation

import java.awt.{ BorderLayout, Dimension }
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*
import javax.swing.text.DefaultCaret

import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.UIUtils

/**
 * Panel displaying the list of active robots and detailed information about the currently selected robot.
 */
class RobotPanel extends JPanel(new BorderLayout()):

  private val listModel = new DefaultListModel[String]()
  private val robotList = new JList[String](listModel)
  private val infoArea = new JTextArea(8, 25)
  private val currentIds = new AtomicReference[List[String]](Nil)

  initLayout()

  /**
   * Initializes the visual components with their properties.
   */
  private def initLayout(): Unit =
    robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    infoArea.setEditable(false)
    val infoAreaCaret = infoArea.getCaret()
    infoAreaCaret match
      case c: DefaultCaret => c.setUpdatePolicy(DefaultCaret.NEVER_UPDATE)
    infoArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11))
    infoArea.setBackground(UI.Colors.backgroundLight)
    infoArea.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UI.Colors.border),
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
      ),
    )

    setBackground(UI.Colors.backgroundMedium)
    setBorder(UIUtils.paddedBorder())

    val infoPanel = new JPanel(new BorderLayout())
    infoPanel.setBorder(UIUtils.titledBorder("Robot Information"))
    infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER)
    infoPanel.setPreferredSize(new Dimension(250, 300))

    val listScrollPane = new JScrollPane(robotList)
    listScrollPane.setBorder(UIUtils.titledBorder("Active Robots"))
    listScrollPane.setPreferredSize(new Dimension(250, 250))

    add(infoPanel, BorderLayout.NORTH)
    add(listScrollPane, BorderLayout.CENTER)



  /**
   * Updates the list of robot IDs, preserving selection when possible.
   *
   * @param ids
   *   List of robot IDs to display
   */
  def setRobotIds(ids: List[String]): Unit =
    if ids != currentIds.get then
      updateList(ids, Option(robotList.getSelectedValue))
      currentIds.set(ids)

  /**
   * Updates the list model with new IDs.
   *
   * @param ids
   *   New list of robot IDs
   * @param previousSelection
   *   Previously selected ID to restore if still present
   */
  private def updateList(ids: List[String], previousSelection: Option[String]): Unit =
    listModel.clear()
    ids.foreach(listModel.addElement)
    previousSelection.filter(ids.contains).foreach(robotList.setSelectedValue(_, true))

  /**
   * Gets the currently selected robot ID.
   *
   * @return
   *   Option containing the selected ID, None if no selection
   */
  def selectedId: Option[String] = Option(robotList.getSelectedValue)

  /**
   * Programmatically selects a robot by ID.
   *
   * @param id
   *   The robot ID to select
   */
  def selectRobot(id: String): Unit = robotList.setSelectedValue(id, true)

  /**
   * Updates the information display area.
   *
   * @param text
   *   The text to display in the info area
   */
  def setInfo(text: String): Unit =
    infoArea.setText(text)

  /**
   * Registers a callback for selection change events.
   *
   * @param callback
   *   Function to call when selection changes
   */
  def onSelectionChanged(callback: () => Unit): Unit =
    robotList.addListSelectionListener { e =>
      if !e.getValueIsAdjusting then callback()
    }
end RobotPanel
