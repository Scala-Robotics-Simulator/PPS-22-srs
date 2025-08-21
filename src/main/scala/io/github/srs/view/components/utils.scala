package io.github.srs.view.components

import java.awt.{ Dimension, Toolkit }
import javax.swing.JFrame

extension (frame: JFrame)

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
   * Sets a fixed size for the frame.
   *
   * @param width
   *   The width of the frame.
   * @param height
   *   The height of the frame.
   */
  def setPreSize(width: Int, height: Int): Unit =
    frame.setPreferredSize(new Dimension(width, height))
    frame.setMinimumSize(new Dimension(width, height))
    frame.setMaximumSize(new Dimension(width, height))
    frame.pack()
end extension
