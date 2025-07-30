package io.github.srs.view

import javax.swing.*
import java.awt.*
import io.github.srs.model.ModelModule
import monix.eval.Task

class SimpleView[S <: ModelModule.State]:
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

  def render(state: S): Task[Unit] =
    Task(setLabelText(s"$state"))

  def close(): Unit =
    frame.dispose()
