package io.github.srs.view.components.simulation

import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.Box.createRigidArea
import javax.swing.JPanel
import javax.swing.border.{ LineBorder, TitledBorder }

/**
 * Panel containing simulation controls grouped by function.
 */
class ControlsPanel extends JPanel:

  import java.awt.{ Color, Cursor, Dimension, FlowLayout, Font, Insets }
  import javax.swing.*
  val startStopButton: JButton = createStyledButton("Start", "\u25B6")
  val pauseResumeButton: JButton = createStyledButton("Pause", "\u23F8")
  private val slowButton = new JRadioButton("Slow")
  private val normalButton = new JRadioButton("Normal", true)
  private val fastButton = new JRadioButton("Fast")

  List(slowButton, normalButton, fastButton).foreach { btn =>
    btn.setBackground(new Color(245, 245, 245))
    btn.setFocusPainted(false)
  }

  /**
   * Creates a component group with a specific title.
   *
   * @param title
   *   The title to display in the group's border
   * @param components
   *   The components to include in the group
   * @return
   *   A panel containing the grouped components with a titled border
   */
  private def createGroup(title: String)(components: JComponent*): JPanel =
    val panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10))
    panel.setBackground(new Color(245, 245, 245))
    val border = BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
      title,
      TitledBorder.LEFT,
      TitledBorder.TOP,
      new Font("Arial", Font.BOLD, 12),
      new Color(60, 60, 60),
    )
    panel.setBorder(
      BorderFactory.createCompoundBorder(
        border,
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
      ),
    )
    components.foreach(panel.add)
    panel

  /**
   * Creates a styled button with text and icon.
   *
   * The button includes hover and press effects for better user feedback.
   *
   * @param text
   *   The text to display on the button
   * @param icon
   *   The Unicode character to use as an icon
   * @return
   *   A styled JButton with hover effects
   */
  private def createStyledButton(text: String, icon: String): JButton =
    val button = new JButton(s"$icon $text")
    button.setFocusPainted(false)
    button.setBorder(new LineBorder(Color.GRAY, 1, true))
    button.setBackground(Color.WHITE)
    button.setPreferredSize(Dimension(150, 30))
    button.setMargin(new Insets(5, 10, 5, 10))
    button.setOpaque(true)
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    button.addMouseListener(new MouseAdapter():
      override def mouseEntered(e: MouseEvent): Unit =
        button.setBackground(new Color(230, 230, 230))

      override def mouseExited(e: MouseEvent): Unit =
        button.setBackground(Color.WHITE)

      override def mousePressed(e: MouseEvent): Unit =
        button.setBackground(new Color(220, 235, 250)))
    button

  private val speedGroup = new ButtonGroup()
  List(slowButton, normalButton, fastButton).foreach(speedGroup.add)

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(new Color(250, 250, 250))
  setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))

  add(createRigidArea(new Dimension(0, 10)))
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  add(createGroup("Simulation")(startStopButton, pauseResumeButton))
  add(createGroup("Speed")(slowButton, normalButton, fastButton))
end ControlsPanel
