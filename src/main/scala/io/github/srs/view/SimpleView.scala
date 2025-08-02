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
  private val btnStart = new JButton("Start")

  private def setLabelText(text: String): Unit =
    lblText.setText(text)

  def init(queue: ConcurrentQueue[Task, Event]): Task[Unit] = Task:
    frame.setMinimumSize(new Dimension(800, 600))
    frame.getContentPane.add(lblText)
    frame.getContentPane.add(btnStart, BorderLayout.SOUTH)
    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

    btnStart.addActionListener { _ =>
      queue.offer(Event.Increment).runAsyncAndForget
    }

  def render(state: S): Task[Unit] =
    Task(setLabelText(s"$state"))

  def close(): Unit =
    frame.dispose()
end SimpleView
