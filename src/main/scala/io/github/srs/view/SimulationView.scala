package io.github.srs.view

import java.awt.{ BorderLayout, Dimension }
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import io.github.srs.controller.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.utils.SimulationDefaults
import io.github.srs.view.components.centerFrame
import io.github.srs.view.components.simulation.{ ControlsPanel, RobotPanel, SimulationCanvas }

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
  /**
   * Factory method to create a new instance of [[SimulationView]].
   *
   * @tparam S
   *   Type of the simulation state handled by the view.
   * @return
   *   A new instance of [[SimulationView]].
   */
  def apply[S <: ModelModule.State](): SimulationView[S] = new SimulationViewImpl[S]

  private class SimulationViewImpl[S <: ModelModule.State] extends SimulationView[S]:
    import SimulationDefaults.{ Frame, Layout }

    private val frame = new JFrame("Scala Robotics Simulator")
    private val canvas = new SimulationCanvas
    private val robotPanel = new RobotPanel
    private val controls = new ControlsPanel
    private val robots = new AtomicReference[List[Robot]](Nil)

    /**
     * Initializes the user interface and sets up event handlers.
     *
     * @param queue
     *   Queue used to publish user-generated events.
     */
    override def init(queue: Queue[IO, Event]): IO[Unit] = IO:
      setupUI()
      setupEventHandlers(queue)
      frame.setVisible(true)

    /**
     * Renders the current simulation state by updating the canvas and robot panel.
     *
     * @param state
     *   Current simulation state.
     */
    override def render(state: S): IO[Unit] = IO:
      val currentRobots = extractRobots(state)
      robots.set(currentRobots)

      SwingUtilities.invokeLater(() =>
        robotPanel.setRobotIds(currentRobots.map(_.id.toString))
        canvas.update(state.environment, currentRobots, robotPanel.selectedId)
        updateRobotInfo(),
      )

    /**
     * Closes the user interface and releases resources.
     */
    override def close(): IO[Unit] = IO(frame.dispose())

    /**
     * Sets up the main user interface components.
     */
    private def setupUI(): Unit =
      canvas.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, Frame.canvasBorder))
      frame.setMinimumSize(new Dimension(Frame.minWidth, Frame.minHeight))
      frame.setPreferredSize(new Dimension(Frame.prefWidth, Frame.prefHeight))

      val sidePanel = createSidePanel()
      val splitPane = createSplitPane(canvas, sidePanel)
      frame.add(splitPane, BorderLayout.CENTER)
      frame.pack()
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      frame.centerFrame()

    /**
     * Creates the side panel containing the robot panel and controls panel.
     *
     * @return
     *   A `JPanel` containing the side panel components.
     */
    private def createSidePanel(): JPanel =
      val panel = new JPanel(new BorderLayout())
      panel.add(robotPanel, BorderLayout.CENTER)
      panel.add(controls, BorderLayout.SOUTH)
      panel

    /**
     * Creates a split pane to separate the canvas and side panel.
     *
     * @param left
     *   The left component of the split pane (canvas).
     * @param right
     *   The right component of the split pane (side panel).
     * @return
     *   A `JSplitPane` configured with the provided components.
     */
    private def createSplitPane(left: JComponent, right: JComponent): JSplitPane =
      val split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right)
      split.setResizeWeight(Layout.splitPaneWeight)
      split.setDividerLocation(Layout.splitPaneLocation)
      split

    /**
     * Sets up event handlers for the controls and robot panel.
     *
     * @param queue
     *   Queue used to publish user-generated events.
     */
    private def setupEventHandlers(queue: Queue[IO, Event]): Unit =
      setupControlButtons(queue)
      robotPanel.onSelectionChanged(() => updateRobotInfo())
      canvas.addSelectionListener(robotPanel.selectRobot)

    /**
     * Configures the control buttons (start, stop, pause, resume).
     *
     * @param queue
     *   Queue used to publish user-generated events.
     */
    private def setupControlButtons(queue: Queue[IO, Event]): Unit =
      controls.pauseResumeButton.setEnabled(false)

      controls.startStopButton.addActionListener { _ =>
        if controls.startStopButton.getText.startsWith("\u25B6") then handleStart(queue)
        else handleStop(queue)
      }

      controls.pauseResumeButton.addActionListener { _ =>
        if controls.pauseResumeButton.getText.startsWith("\u23F8") then handlePause(queue)
        else handleResume(queue)
      }

    /**
     * Handles the start button action.
     *
     * @param queue
     *   Queue used to publish the start event.
     */
    private def handleStart(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Resume).unsafeRunAndForget()
      controls.startStopButton.setText("\u23F9 Stop")
      controls.pauseResumeButton.setEnabled(true)

    /**
     * Handles the stop button action.
     *
     * @param queue
     *   Queue used to publish the stop event.
     */
    private def handleStop(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Stop).unsafeRunAndForget()
      controls.startStopButton.setText("\u25B6  Start")
      controls.pauseResumeButton.setText("\u23F8 Pause")
      controls.pauseResumeButton.setEnabled(false)

    /**
     * Handles the pause button action.
     *
     * @param queue
     *   Queue used to publish the pause event.
     */
    private def handlePause(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Pause).unsafeRunAndForget()
      controls.pauseResumeButton.setText("\u25B6 Resume")

    /**
     * Handles the resume button action.
     *
     * @param queue
     *   Queue used to publish the resume event.
     */
    private def handleResume(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Resume).unsafeRunAndForget()
      controls.pauseResumeButton.setText("\u23F8 Pause")

    /**
     * Extracts the list of robots from the simulation state.
     *
     * @param state
     *   Current simulation state.
     * @return
     *   A sorted list of robots.
     */
    private def extractRobots(state: S): List[Robot] =
      state.environment.entities.collect { case r: Robot => r }.toList
        .sortBy(_.id.toString)

    /**
     * Updates the robot information panel with details of the selected robot.
     */
    private def updateRobotInfo(): Unit =
      val infoText = robotPanel.selectedId
        .flatMap(id => robots.get.find(_.id.toString == id))
        .map(formatRobotInfo)
        .getOrElse("Select a robot")

      robotPanel.setInfo(infoText)

    /**
     * Formats the information of a robot for display in the robot panel.
     *
     * @param robot
     *   The robot whose information is to be formatted.
     * @return
     *   A formatted string containing the robot's details.
     */
    private def formatRobotInfo(robot: Robot): String =
      val id = robot.id.toString.take(5)
      val (x, y) = (robot.position.x, robot.position.y)
      val orientation = robot.orientation.degrees
      val sensors = robot.sensors.size

      f"Robot ID: $id%n" +
        f"Position: ($x%.2f, $y%.2f)%n" +
        f"Orientation: $orientation%.2f°%n" +
        f"N.Sensors: $sensors"
  end SimulationViewImpl
end SimulationView
