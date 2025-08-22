package io.github.srs.view.components.simulation

import java.awt.{ BorderLayout, Dimension }
import java.util.concurrent.atomic.AtomicReference
import javax.swing.{ BorderFactory, JPanel }

import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.UIStyles

/**
 * Panel displaying the list of robots and their related information.
 */
class RobotPanel extends JPanel(new BorderLayout()):

  import io.github.srs.utils.SimulationDefaults.UI.Dimensions

  import javax.swing.{ DefaultListModel, JList, JScrollPane, JTextArea, ListSelectionModel }

  private val listModel = new DefaultListModel[String]()
  private val robotList = new JList[String](listModel)
  private val infoArea = new JTextArea(Dimensions.infoAreaRows, Dimensions.infoAreaColumns)
  private val currentIds = new AtomicReference[List[String]](Nil)

  // Initialize components using consistent styling
  private def initComponents(): Unit =
    robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    infoArea.setEditable(false)
    infoArea.setBackground(UI.Colors.backgroundLight)
    infoArea.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UI.Colors.border),
        UIStyles.paddedBorder(),
      ),
    )

  private def createScrollPane(): JScrollPane =
    val pane = new JScrollPane(robotList)
    pane.setBorder(UIStyles.titledBorder("Active Robots"))
    pane.setPreferredSize(new Dimension(Dimensions.robotListWidth, Dimensions.robotListHeight))
    pane

  private def createInfoPanel(): JPanel =
    val panel = new JPanel(new BorderLayout())
    panel.setBorder(UIStyles.titledBorder("Robot Information"))
    panel.add(infoArea, BorderLayout.CENTER)
    panel

  // Initialize the panel
  initComponents()
  setBackground(UI.Colors.backgroundMedium)
  setBorder(UIStyles.paddedBorder())
  add(createScrollPane(), BorderLayout.CENTER)
  add(createInfoPanel(), BorderLayout.SOUTH)

  /**
   * Updates robot list preserving selection when possible.
   */
  def setRobotIds(ids: List[String]): Unit =
    if ids != currentIds.get then
      updateList(ids, Option(robotList.getSelectedValue))
      currentIds.set(ids)

  private def updateList(ids: List[String], previousSelection: Option[String]): Unit =
    listModel.clear()
    ids.foreach(listModel.addElement)
    previousSelection.filter(ids.contains).foreach(robotList.setSelectedValue(_, true))

  def selectedId: Option[String] = Option(robotList.getSelectedValue)

  def selectRobot(id: String): Unit = robotList.setSelectedValue(id, true)

  def setInfo(text: String): Unit = infoArea.setText(text)

  def onSelectionChanged(callback: () => Unit): Unit =
    robotList.addListSelectionListener(_ => if !robotList.getValueIsAdjusting then callback())
end RobotPanel
