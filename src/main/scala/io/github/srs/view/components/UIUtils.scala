package io.github.srs.view.components

import java.awt.{Dimension, Toolkit}
import javax.swing.BorderFactory
import javax.swing.border.TitledBorder
import io.github.srs.utils.SimulationDefaults.{Frame, UI}

/**
 * Centralized UI styling to ensure consistency across components
 */
object UIUtils:

  def titledBorder(title: String): javax.swing.border.Border =
    BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(UI.Colors.border),
        title,
        TitledBorder.LEFT,
        TitledBorder.TOP,
      ),
      BorderFactory.createEmptyBorder(
        UI.Spacing.innerPadding,
        UI.Spacing.innerPadding,
        UI.Spacing.innerPadding,
        UI.Spacing.innerPadding,
      ),
    )

  def paddedBorder(padding: Int = UI.Spacing.standardPadding): javax.swing.border.Border =
    BorderFactory.createEmptyBorder(padding, padding, padding, padding)

end UIUtils

extension (frame: javax.swing.JFrame)

  /**
   * Centers the frame on the screen.
   */
  def centerFrame(): Unit =
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val frameSize = frame.getSize
    val x = (screenSize.width - frameSize.width) / 2
    val y = (screenSize.height - frameSize.height) / 2
    frame.setLocation(x, y)

  /**
   * Applies default and preferred size to the frame.
   */
  def applyDefaultAndPreferSize(): Unit =
      frame.setMinimumSize(new Dimension(Frame.minWidth, Frame.minHeight))
      frame.setPreferredSize(new Dimension(Frame.prefWidth, Frame.prefHeight))
