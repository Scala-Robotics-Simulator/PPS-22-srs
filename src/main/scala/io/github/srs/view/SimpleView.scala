package io.github.srs.view

import java.awt.{ Dimension, Font }
import javax.swing.*


/**
 * The [[SimpleView]] provides a basic GUI setup to visualize formatted data in a non-editable text area.
 * It is primarily used as part of the view implementation in the [[ViewModule]], offering methods to
 * initialize the GUI, display data, and properly close the window.
 */
class SimpleView:

  private val frame  = new JFrame("Scala Robotics Simulator")
  private val area   = new JTextArea()
  private val pane   =
    new JScrollPane(
      area,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
    )


  def init(): Unit =
    area.setEditable(false)
    area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14))
    frame.setMinimumSize(new Dimension(820, 640))
    frame.getContentPane.add(pane)
    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)


  def plotData(fullDump: String): Unit =
    area.setText(fullDump)
    area.setCaretPosition(0)                             // jump to top

  def close(): Unit = frame.dispose()
end SimpleView
