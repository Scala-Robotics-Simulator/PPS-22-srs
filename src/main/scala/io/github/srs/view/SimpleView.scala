package io.github.srs.view

import java.awt.*
import javax.swing.*

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import io.github.srs.controller.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.utils.time.TimeUtils.formatTime

class SimpleView[S <: ModelModule.State]:
  private val frame = new JFrame("Scala Robotics Simulator")
  private val lblText = new JLabel("Hello World!", SwingConstants.CENTER)
  private val lblTimeElapsed = new JLabel("Elapsed Time: 00:00", SwingConstants.CENTER)
  private val rbtnSlow = new JRadioButton("Slow")
  private val rbtnNormal = new JRadioButton("Normal", true)
  private val rbtnFast = new JRadioButton("Fast")
  private val btnIncrement = new JButton("Increment")
  private val btnPause = new JButton("Pause")
  private val btnResume = new JButton("Resume")
  private val btnStop = new JButton("Stop")

  def init(queue: Queue[IO, Event]): IO[Unit] = IO:
    frame.setMinimumSize(new Dimension(800, 600))
    frame.setLayout(new BorderLayout())

    val contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS))
    lblText.setAlignmentX(Component.CENTER_ALIGNMENT)
    lblTimeElapsed.setAlignmentX(Component.CENTER_ALIGNMENT)
    contentPanel.add(lblText, BorderLayout.CENTER)
    contentPanel.add(lblTimeElapsed, BorderLayout.CENTER)
    frame.getContentPane.add(contentPanel, BorderLayout.CENTER)

    val buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
    buttonPanel.add(btnIncrement)
    buttonPanel.add(btnPause)
    buttonPanel.add(btnResume)
    buttonPanel.add(btnStop)
    frame.getContentPane.add(buttonPanel, BorderLayout.SOUTH)

    val speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
    speedPanel.setBorder(BorderFactory.createTitledBorder("Speed"))
    val speedGroup = new ButtonGroup()
    speedGroup.add(rbtnSlow)
    speedGroup.add(rbtnNormal)
    speedGroup.add(rbtnFast)
    speedPanel.add(rbtnSlow)
    speedPanel.add(rbtnNormal)
    speedPanel.add(rbtnFast)
    frame.getContentPane.add(speedPanel, BorderLayout.NORTH)

    frame.pack()
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setVisible(true)

    rbtnSlow.addActionListener { _ =>
      queue.offer(Event.TickSpeed(SimulationSpeed.SLOW)).unsafeRunAndForget()
    }
    rbtnNormal.addActionListener { _ =>
      queue.offer(Event.TickSpeed(SimulationSpeed.NORMAL)).unsafeRunAndForget()
    }
    rbtnFast.addActionListener { _ =>
      queue.offer(Event.TickSpeed(SimulationSpeed.FAST)).unsafeRunAndForget()
    }

    btnIncrement.addActionListener { _ =>
      queue.offer(Event.Increment).unsafeRunAndForget()
    }

    btnPause.addActionListener { _ =>
      queue.offer(Event.Pause).unsafeRunAndForget()
    }

    btnResume.addActionListener { _ =>
      queue.offer(Event.Resume).unsafeRunAndForget()
    }

    btnStop.addActionListener { _ =>
      queue.offer(Event.Stop).unsafeRunAndForget()
    }

  def render(state: S): IO[Unit] = IO:
    val text = state.simulationTime match
      case Some(max) =>
        val remaining = max - state.elapsedTime
        s"Remaining time: ${formatTime(remaining)}"
      case None =>
        s"Elapsed time: ${formatTime(state.elapsedTime)}"

    SwingUtilities.invokeLater(() =>
      lblText.setText(s"$state")
      lblTimeElapsed.setText(text),
    )

  def close(): Unit =
    frame.dispose()
end SimpleView
