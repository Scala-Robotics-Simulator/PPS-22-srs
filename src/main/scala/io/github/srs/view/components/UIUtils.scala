package io.github.srs.view.components

import java.awt.{ Dimension, Font, Toolkit }
import javax.swing.BorderFactory
import javax.swing.border.TitledBorder
import java.util.Locale

import io.github.srs.utils.SimulationDefaults.{ Frame, UI }

/**
 * Centralized UI styling to ensure consistency across components
 */
object UIUtils:

  def titledBorder(title: String, spacing: Int = UI.Spacing.InnerPadding): javax.swing.border.Border =
    val titledBorder = BorderFactory.createTitledBorder(
      BorderFactory.createLineBorder(UI.Colors.border),
      title.toUpperCase(Locale.getDefault()),
      TitledBorder.LEFT,
      TitledBorder.TOP,
    )

    val currentFont = titledBorder.getTitleFont
    val newFont = new Font(
      currentFont.getFamily,
      currentFont.getStyle,
      UI.Fonts.TitleSize,
    )
    titledBorder.setTitleFont(newFont)

    BorderFactory.createCompoundBorder(
      titledBorder,
      BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing),
    )

  end titledBorder

  def paddedBorder(padding: Int = UI.Spacing.StandardPadding): javax.swing.border.Border =
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
    frame.setMinimumSize(new Dimension(Frame.MinWidth, Frame.MinHeight))
    frame.setPreferredSize(new Dimension(Frame.PrefWidth, Frame.PrefHeight))
