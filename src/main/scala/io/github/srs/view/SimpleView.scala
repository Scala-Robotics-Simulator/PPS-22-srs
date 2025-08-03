package io.github.srs.view

import javax.swing.*
import java.awt.*

import io.github.srs.model.ModelModule
import monix.catnap.ConcurrentQueue
import monix.eval.Task
import io.github.srs.controller.Event
import monix.execution.Scheduler.Implicits.global

class SimpleView[S <: ModelModule.State]:
  private val frame = new JFrame("Scala Robotics Simulator")
  private val lblText = new JLabel("Hello World!", SwingConstants.CENTER)
  private val btnIncrement = new JButton("Increment")
  private val btnPause = new JButton("Pause")
  private val btnResume = new JButton("Resume")
  private val btnStop = new JButton("Stop")

  private def setLabelText(text: String): Unit =
    lblText.setText(text)

  def init(queue: ConcurrentQueue[Task, Event]): Task[Unit] = Task:
    frame.setMinimumSize(new Dimension(800, 600))
    frame.setLayout(new BorderLayout())
    frame.getContentPane.add(lblText, BorderLayout.CENTER)

    val buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
    buttonPanel.add(btnIncrement)
    buttonPanel.add(btnPause)
    buttonPanel.add(btnResume)
    buttonPanel.add(btnStop)
    frame.getContentPane.add(buttonPanel, BorderLayout.SOUTH)

    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

    btnIncrement.addActionListener { _ =>
      queue.offer(Event.Increment).runAsyncAndForget
    }

    btnPause.addActionListener { _ =>
      queue.offer(Event.Pause).runAsyncAndForget
    }

    btnResume.addActionListener { _ =>
      queue.offer(Event.Resume).runAsyncAndForget
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
