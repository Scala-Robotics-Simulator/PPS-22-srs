package io.github.srs.view

import javax.swing.*
import java.awt.*

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import io.github.srs.model.ModelModule
import monix.catnap.ConcurrentQueue
import monix.eval.Task
import io.github.srs.controller.Event
import monix.execution.Scheduler.Implicits.global

class SimpleView[S <: ModelModule.State]:
  private val frame = new JFrame("Scala Robotics Simulator")
  private val simulationTime = new JTextField(3)
  private val lblText = new JLabel("Hello World!", SwingConstants.CENTER)
  private val btnChangeTime = new JButton("Change Time")
  private val btnIncrement = new JButton("Increment")
  private val btnStop = new JButton("Stop")

  private def setLabelText(text: String): Unit =
    lblText.setText(text)

  private def readSimulationDuration: FiniteDuration =
    val text = simulationTime.getText.trim
    Integer.parseInt(text) match
      case duration if duration > 0 => duration.millis

  def init(queue: ConcurrentQueue[Task, Event]): Task[Unit] = Task:
    frame.setMinimumSize(new Dimension(800, 600))
    frame.setLayout(new BorderLayout())
    frame.getContentPane.add(simulationTime, BorderLayout.NORTH)
    frame.getContentPane.add(lblText, BorderLayout.CENTER)

    val buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
    buttonPanel.add(btnChangeTime)
    buttonPanel.add(btnIncrement)
    buttonPanel.add(btnStop)
    frame.getContentPane.add(buttonPanel, BorderLayout.SOUTH)

    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

    btnChangeTime.addActionListener { _ =>
      println(readSimulationDuration)
      queue.offer(Event.ChangeTime(readSimulationDuration)).runAsyncAndForget
    }

    btnIncrement.addActionListener { _ =>
      queue.offer(Event.Increment).runAsyncAndForget
    }

    btnStop.addActionListener { _ =>
      queue.offer(Event.Stop).runAsyncAndForget
    }

  def render(state: S): Task[Unit] =
    Task:
      SwingUtilities.invokeLater(() => setLabelText(s"$state"))

  def close(): Unit =
    frame.dispose()
end SimpleView
