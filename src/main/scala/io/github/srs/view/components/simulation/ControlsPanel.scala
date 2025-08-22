package io.github.srs.view.components.simulation

import javax.swing.JPanel

import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.UIStyles

/**
 * Panel containing simulation controls grouped by function.
 */
class ControlsPanel extends JPanel:

  import io.github.srs.utils.SimulationDefaults.UI.{ Colors, Dimensions, Icons }

  import java.awt.event.{ MouseAdapter, MouseEvent }
  import java.awt.{ Color, Cursor, Dimension, FlowLayout, Insets }
  import javax.swing.border.LineBorder
  import javax.swing.{ Box, BoxLayout, ButtonGroup, JButton, JComponent, JRadioButton }

  val startStopButton: JButton = createActionButton("Start", "\u25B6")
  val pauseResumeButton: JButton = createActionButton("Pause", "\u23F8")

  private val (slowButton, normalButton, fastButton) = createSpeedControls()

  def onSpeedChanged(callback: String => Unit): Unit =
    List(slowButton, normalButton, fastButton).foreach { btn =>
      btn.addActionListener(_ => callback(getSelectedSpeed))
    }

  def updateButtonText(isRunning: Boolean, isPaused: Boolean): Unit =
    startStopButton.setText(
      if isRunning then s"${Icons.stop} Stop"
      else s"${Icons.play} Start",
    )
    pauseResumeButton.setText(
      if isPaused then s"${Icons.play} Resume"
      else s"${Icons.pause} Pause",
    )

  def getSelectedSpeed: String =
    if slowButton.isSelected then "slow"
    else if fastButton.isSelected then "fast"
    else "normal"

  private def createActionButton(text: String, icon: String): JButton =
    val button = new JButton(s"$icon $text")
    styleButton(button)
    addHoverEffect(button)
    button

  private def addHoverEffect(button: JButton): Unit =
    button.addMouseListener(new MouseAdapter():
      override def mouseEntered(e: MouseEvent): Unit =
        button.setBackground(Colors.buttonHover)

      override def mouseExited(e: MouseEvent): Unit =
        button.setBackground(Color.WHITE)

      override def mousePressed(e: MouseEvent): Unit =
        button.setBackground(Colors.buttonPressed))

  private def createSpeedControls(): (JRadioButton, JRadioButton, JRadioButton) =
    val slowBtn = new JRadioButton("Slow", false)
    val normalBtn = new JRadioButton("Normal", true)
    val fastBtn = new JRadioButton("Fast", false)

    val buttons = List(slowBtn, normalBtn, fastBtn)
    buttons.foreach { btn =>
      btn.setBackground(UI.Colors.backgroundMedium)
      btn.setFocusPainted(false)
    }

    val group = new ButtonGroup()
    buttons.foreach(group.add)

    (slowBtn, normalBtn, fastBtn)

  private def createControlGroup(title: String, components: JComponent*): JPanel =
    val panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10))
    panel.setBackground(UI.Colors.backgroundMedium)
    panel.setBorder(UIStyles.titledBorder(title))
    components.foreach(panel.add)
    panel

  private def styleButton(button: JButton): Unit =
    button.setFocusPainted(false)
    button.setBorder(new LineBorder(Color.GRAY, 1, true))
    button.setBackground(Color.WHITE)
    button.setPreferredSize(new Dimension(Dimensions.buttonWidth, Dimensions.buttonHeight))
    button.setMargin(new Insets(5, 10, 5, 10))
    button.setOpaque(true)
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))

  // Initialize layout
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(UI.Colors.backgroundLight)
  setBorder(UIStyles.paddedBorder())

  add(Box.createRigidArea(new Dimension(0, 10)))
  add(createControlGroup("Simulation", startStopButton, pauseResumeButton))
  add(createControlGroup("Speed", slowButton, normalButton, fastButton))
end ControlsPanel
