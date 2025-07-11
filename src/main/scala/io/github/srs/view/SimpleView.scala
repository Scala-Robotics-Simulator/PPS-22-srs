package io.github.srs.view

import javax.swing.*
import java.awt.*

class SimpleView:
  private val frame = new JFrame("Scala Robotics Simulator")
  private val lblText = new JLabel("Hello World!", SwingConstants.CENTER)

  def init(): Unit =
    frame.setMinimumSize(new Dimension(800, 600))
    frame.getContentPane.add(lblText)
    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

  private def setLabelText(text: String): Unit =
    lblText.setText(text)

  def plotData(data: Int): Unit =
    setLabelText(s"${lblText.getText} ${data.toString}")

  def close(): Unit =
    frame.dispose()
