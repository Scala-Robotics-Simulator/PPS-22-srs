package io.github.srs.view

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.SimulationConfig.SimulationSpeed
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.view.components.simulation.{ControlsPanel, RobotPanel, SimulationCanvas}
import io.github.srs.view.state.SimulationViewState

import java.awt.{BorderLayout, Dimension}
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

/**
 * Abstraction for the simulation user interface.
 *
 * @tparam S
 *   type of the simulation state handled by the view
 */
trait SimulationView[S <: ModelModule.State]:

  /**
   * Initializes and displays the view.
   *
   * @param queue
   *   queue used to publish user generated events
   */
  def init(queue: Queue[IO, Event]): IO[Unit]

  /**
   * Renders the current state of the simulation.
   *
   * @param state
   *   current simulation state
   */
  def render(state: S): IO[Unit]

  /**
   * Closes the view and releases resources.
   */
  def close(): IO[Unit]

end SimulationView

object SimulationView:
  def apply[S <: ModelModule.State](): SimulationView[S] = new SimulationViewImpl[S]

  private class SimulationViewImpl[S <: ModelModule.State] extends SimulationView[S]:
    import io.github.srs.utils.SimulationDefaults.{ Frame, Layout }

    private val frame = new JFrame("Scala Robotics Simulator")
    private val canvas = new SimulationCanvas
    private val robotPanel = new RobotPanel
    private val controls = new ControlsPanel
    private val viewState = new AtomicReference(SimulationViewState())

    override def init(queue: Queue[IO, Event]): IO[Unit] = IO:
      setupUI()
      setupEventHandlers(queue)
      frame.setVisible(true)

    override def render(state: S): IO[Unit] = IO:
      val newState = viewState.updateAndGet(_.withEnvironment(state.environment))
      SwingUtilities.invokeLater(() =>
        robotPanel.setRobotIds(newState.robots.map(_.id.toString))
        canvas.update(state.environment, robotPanel.selectedId)
        updateRobotInfo(),
      )

    override def close(): IO[Unit] = IO(frame.dispose())

    private def setupUI(): Unit =
      import io.github.srs.view.components.centerFrame

      canvas.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, Frame.canvasBorder))
      frame.setMinimumSize(new Dimension(Frame.minWidth, Frame.minHeight))
      frame.setPreferredSize(new Dimension(Frame.prefWidth, Frame.prefHeight))
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

      val splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        canvas,
        createSidePanel(),
      )
      splitPane.setResizeWeight(Layout.splitPaneWeight)
      splitPane.setDividerLocation(Layout.splitPaneLocation)

      frame.add(splitPane, BorderLayout.CENTER)
      frame.pack()
      frame.centerFrame()

    private def createSidePanel(): JPanel =
      val panel = new JPanel(new BorderLayout())
      panel.add(robotPanel, BorderLayout.CENTER)
      panel.add(controls, BorderLayout.SOUTH)
      panel

    private def setupEventHandlers(queue: Queue[IO, Event]): Unit =
      setupControlHandlers(queue)
      robotPanel.onSelectionChanged(() => updateRobotInfo())
      canvas.addSelectionListener(robotPanel.selectRobot)

    private def setupControlHandlers(queue: Queue[IO, Event]): Unit =
      controls.pauseResumeButton.setEnabled(false)

      controls.startStopButton.addActionListener { _ =>
        val isStart = controls.startStopButton.getText.startsWith("\u25B6")
        handleSimulationControl(queue, isStart)
      }

      controls.pauseResumeButton.addActionListener { _ =>
        val isPause = controls.pauseResumeButton.getText.startsWith("\u23F8")
        handlePauseResume(queue, isPause)
      }

      controls.onSpeedChanged { speed =>
        val simulationSpeed = parseSpeed(speed)
        queue.offer(Event.TickSpeed(simulationSpeed)).unsafeRunAndForget()
      }

    private def parseSpeed(speed: String): SimulationSpeed = speed match
      case "slow" => SimulationSpeed.SLOW
      case "fast" => SimulationSpeed.FAST
      case _ => SimulationSpeed.NORMAL

    private def handleSimulationControl(queue: Queue[IO, Event], isStart: Boolean): Unit =
      if isStart then
        startSimulation(queue)
      else
        showStopConfirmation(queue)

    private def startSimulation(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Resume).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = false)
      controls.pauseResumeButton.setEnabled(true)

    private def showStopConfirmation(queue: Queue[IO, Event]): Unit =
      // First pause the simulation before showing dialog
      queue.offer(Event.Pause).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = true)

      val result = JOptionPane.showConfirmDialog(
        frame,
        "Are you sure you want to stop the simulation?\n\nClick Yes to stop, No to continue.",
        "Stop Simulation",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
      )

      if result == JOptionPane.YES_OPTION then
        // User confirmed stop
        queue.offer(Event.Stop).unsafeRunAndForget()
        controls.updateButtonText(isRunning = false, isPaused = false)
        controls.pauseResumeButton.setEnabled(false)
      else
        // User cancelled - resume the simulation
        queue.offer(Event.Resume).unsafeRunAndForget()
        controls.updateButtonText(isRunning = true, isPaused = false)

    private def handlePauseResume(queue: Queue[IO, Event], isPause: Boolean): Unit =
      val event = if isPause then Event.Pause else Event.Resume
      queue.offer(event).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = isPause)

    private def updateRobotInfo(): Unit =
      val currentState = viewState.get
      val info = for {
        selectedId <- robotPanel.selectedId
        robot <- currentState.robots.find(_.id.toString == selectedId)
      } yield formatRobotInfo(robot)

      robotPanel.setInfo(info.getOrElse("Select a robot to view details"))

    private def formatRobotInfo(robot: Robot): String =
      s"""Robot ID: ${robot.id.toString.take(8)}...
         |Position: (${f"${robot.position.x}%.2f"}, ${f"${robot.position.y}%.2f"})
         |Orientation: ${f"${robot.orientation.degrees}%.1f"}°
         |N.Sensors: ${robot.sensors.size}""".stripMargin

