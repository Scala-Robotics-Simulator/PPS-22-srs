package io.github.srs.view.components.simulation

import javax.swing.*
import java.awt.*
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.border.LineBorder

import scala.collection.immutable.List

import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.UIUtils

/**
 * Control panel containing simulation controls (start/stop, pause/resume) and speed adjustment options.
 */
class ControlsPanel extends JPanel:
  import UI.{ Colors, Dimensions, Icons }

  /** Button for starting and stopping the simulation */
  val startStopButton: JButton = createActionButton("Start", Icons.play)

  /** Button for pausing and resuming the simulation */
  val pauseResumeButton: JButton = createActionButton("Pause", Icons.pause)

  private val (slowButton, normalButton, fastButton) = createSpeedControls()

  initLayout()

  /**
   * Registers a callback for speed change events.
   *
   * @param callback
   *   Function to call with the selected speed ("slow", "normal", or "fast")
   */
  def onSpeedChanged(callback: String => Unit): Unit =
    List(slowButton, normalButton, fastButton).foreach { btn =>
      btn.addActionListener(_ => callback(getSelectedSpeed))
    }

  /**
   * Updates button text based on simulation state.
   *
   * @param isRunning
   *   True if simulation is running
   * @param isPaused
   *   True if simulation is paused
   */
  def updateButtonText(isRunning: Boolean, isPaused: Boolean): Unit =
    startStopButton.setText(
      if isRunning then s"${Icons.stop} Stop"
      else s"${Icons.play} Start",
    )
    pauseResumeButton.setText(
      if isPaused then s"${Icons.play} Resume"
      else s"${Icons.pause} Pause",
    )

  /**
   * Gets the currently selected speed setting.
   *
   * @return
   *   "slow", "fast", or "normal" based on radio button selection
   */
  private def getSelectedSpeed: String =
    if slowButton.isSelected then "slow"
    else if fastButton.isSelected then "fast"
    else "normal"

  /**
   * Creates a styled action button with hover effects.
   *
   * @param text
   *   Button label text
   * @param icon
   *   Icon character to display
   * @return
   *   Configured JButton
   */
  private def createActionButton(text: String, icon: String): JButton =
    val button = new JButton(s"$icon $text")
    styleButton(button)
    addHoverEffect(button)
    button

  /**
   * Adds hover and click visual feedback to a button.
   *
   * @param button
   *   The button to enhance
   */
  private def addHoverEffect(button: JButton): Unit =
    button.addMouseListener(new MouseAdapter:
      override def mouseEntered(e: MouseEvent): Unit =
        button.setBackground(Colors.buttonHover)

      override def mouseExited(e: MouseEvent): Unit =
        button.setBackground(Color.WHITE)

      override def mousePressed(e: MouseEvent): Unit =
        button.setBackground(Colors.buttonPressed))

  /**
   * Creates the speed control radio buttons.
   *
   * @return
   *   Tuple of (slow, normal, fast) radio buttons
   */
  private def createSpeedControls(): (JRadioButton, JRadioButton, JRadioButton) =
    val slowBtn = new JRadioButton("Slow", false)
    val normalBtn = new JRadioButton("Normal", true)
    val fastBtn = new JRadioButton("Fast", false)

    val buttons = List(slowBtn, normalBtn, fastBtn)
    buttons.foreach { btn =>
      btn.setBackground(Colors.backgroundMedium)
      btn.setFocusPainted(false)
    }

    val group = new ButtonGroup()
    buttons.foreach(group.add)

    (slowBtn, normalBtn, fastBtn)

  /**
   * Creates a titled panel group for related controls.
   *
   * @param title
   *   The title for the control group
   * @param components
   *   Components to include in the group
   * @return
   *   Configured JPanel with a titled border
   */
  private def createControlGroup(title: String, components: JComponent*): JPanel =
    val panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10))
    panel.setBackground(Colors.backgroundMedium)
    panel.setBorder(UIUtils.titledBorder(title))
    components.foreach(panel.add)
    panel

  /**
   * Applies consistent styling to a button.
   *
   * @param button
   *   The button to style
   */
  private def styleButton(button: JButton): Unit =
    button.setFocusPainted(false)
    button.setBorder(new LineBorder(Color.GRAY, 1, true))
    button.setBackground(Color.WHITE)
    button.setPreferredSize(new Dimension(Dimensions.buttonWidth, Dimensions.buttonHeight))
    button.setMargin(new Insets(5, 10, 5, 10))
    button.setOpaque(true)
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))

  /**
   * Initializes the panel layout with control groups.
   */
  private def initLayout(): Unit =
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    setBackground(Colors.backgroundLight)
    setBorder(UIUtils.paddedBorder())

    Seq(
      Box.createRigidArea(new Dimension(0, 10)),
      createControlGroup("Simulation", startStopButton, pauseResumeButton),
      createControlGroup("Speed", slowButton, normalButton, fastButton),
    ).foreach(add)

end ControlsPanel
