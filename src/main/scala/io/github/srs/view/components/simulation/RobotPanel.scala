package io.github.srs.view.components.simulation

import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel
import javax.swing.border.TitledBorder

/**
 * Panel displaying the list of robots and their related information.
 */
class RobotPanel extends JPanel(new BorderLayout()):

  import java.awt.{ BorderLayout, Color, Dimension, Font }
  import javax.swing.*
  private val listModel = new DefaultListModel[String]()
  private val robotList = new JList[String](listModel)
  private val infoArea = new JTextArea(5, 20)
  private val lastIds = new AtomicReference[List[String]](Nil)

  robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  infoArea.setEditable(false)
  infoArea.setBackground(new Color(245, 245, 245))

  infoArea.setBorder(
    BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(new Color(200, 200, 200)),
      BorderFactory.createEmptyBorder(10, 10, 10, 10),
    ),
  )

  private val scrollPane = new JScrollPane(robotList)

  scrollPane.setBorder(
    BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(new Color(200, 200, 200)),
      "Active Robots",
      TitledBorder.LEFT,
      TitledBorder.TOP,
      new Font("Arial", Font.BOLD, 12),
      new Color(60, 60, 60),
    ),
  )
  scrollPane.setPreferredSize(new Dimension(200, 300))

  private val infoPanel = new JPanel(new BorderLayout())

  infoPanel.setBorder(
    BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(new Color(200, 200, 200)),
      "Robot Information",
      TitledBorder.LEFT,
      TitledBorder.TOP,
      new Font("Arial", Font.BOLD, 12),
      new Color(60, 60, 60),
    ),
  )
  infoPanel.add(infoArea, BorderLayout.CENTER)

  infoPanel.add(infoArea, BorderLayout.CENTER)

  setBackground(new Color(250, 250, 250))
  setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
  add(scrollPane, BorderLayout.CENTER)
  add(infoPanel, BorderLayout.SOUTH)

  /**
   * Updates the displayed robot identifiers while preserving the current selection.
   *
   * @param ids
   *   the list of robot identifiers to show
   */
  def setRobotIds(ids: List[String]): Unit =
    if ids != lastIds.get then
      val selected = Option(robotList.getSelectedValue)
      listModel.clear()
      ids.foreach(listModel.addElement)
      lastIds.set(ids)
      selected.foreach(id => robotList.setSelectedValue(id, true))

  /**
   * Returns the currently selected robot identifier, if any.
   *
   * @return
   *   An `Option` containing the selected robot identifier, or `None` if no robot is selected.
   */
  def selectedId: Option[String] = Option(robotList.getSelectedValue)

  /**
   * Selects the robot with the given identifier.
   *
   * @param id
   *   The identifier of the robot to select.
   */
  def selectRobot(id: String): Unit = robotList.setSelectedValue(id, true)

  /**
   * Sets the information text shown under the list.
   *
   * @param text
   *   The information text to display.
   */
  def setInfo(text: String): Unit = infoArea.setText(text)

  /**
   * Adds a callback invoked when the selection changes.
   *
   * @param f
   *   A function to execute when the selection changes.
   */
  def onSelectionChanged(f: () => Unit): Unit =
    robotList.addListSelectionListener(_ => f())
end RobotPanel
