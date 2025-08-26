package io.github.srs.view

import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicReference
import javax.swing.*

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import io.github.srs.controller.protocol.Event
import io.github.srs.model.ModelModule
import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.utils.SimulationDefaults.UI
import io.github.srs.view.components.simulation.*
import io.github.srs.view.state.SimulationViewState
import io.github.srs.model.environment.Environment
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import cats.Id
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.prettyPrint

/**
 * Trait defining the interface for the simulation view. Handles UI initialization, rendering, and cleanup.
 *
 * @tparam S
 *   The type of simulation state must extend [[ModelModule.State]]
 */
trait SimulationView[S <: ModelModule.State]:

  /**
   * Initializes the view components and displays the UI.
   *
   * @param queue
   *   Event queue for publishing user-generated events to the controller
   * @return
   *   IO effect that performs the initialization
   */
  def init(queue: Queue[IO, Event]): IO[Unit]

  /**
   * Updates the view with the current simulation state.
   *
   * @param state
   *   Current state of the simulation to be rendered
   * @return
   *   IO effect that performs the rendering
   */
  def render(state: S): IO[Unit]

  /**
   * Closes the view and releases all associated resources.
   *
   * @return
   *   IO effect that performs the cleanup
   */
  def close(): IO[Unit]

end SimulationView

object SimulationView:

  /**
   * Factory method to create a new [[SimulationView]] instance.
   *
   * @tparam S
   *   The type of simulation state
   * @return
   *   A new [[SimulationView]] implementation
   */
  def apply[S <: ModelModule.State](): SimulationView[S] = new SimulationViewImpl[S]

  /**
   * Private implementation of [[SimulationView]] using Swing components.
   *
   * @tparam S
   *   The type of simulation state
   */
  private class SimulationViewImpl[S <: ModelModule.State] extends SimulationView[S]:

    import io.github.srs.utils.SimulationDefaults.{ Frame, Layout }
    import io.github.srs.model.SimulationConfig.SimulationSpeed

    type RobotId = String
    private type SpeedLabel = String

    private val frame = new JFrame("Scala Robotics Simulator")
    private val canvas = new SimulationCanvas
    private val robotPanel = new RobotPanel
    private val timePanel = new TimePanel
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
        timePanel.updateTimes(state.elapsedTime, state.simulationTime)
        updateRobotInfo(),
      )

    override def close(): IO[Unit] = IO(frame.dispose())

    /**
     * Sets up the main UI layout with split pane configuration.
     */
    private def setupUI(): Unit =
      import io.github.srs.view.components.{ centerFrame, applyDefaultAndPreferSize }

      canvas.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, Frame.canvasBorder))
      frame.applyDefaultAndPreferSize()
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

    /**
     * Creates the side panel containing robot info and controls.
     *
     * @return
     *   JPanel with robot panel and control buttons
     */
    private def createSidePanel(): JPanel =
      val panel = new JPanel(new BorderLayout())

      val infoContainer = new JPanel(new BorderLayout())
      infoContainer.add(robotPanel, BorderLayout.CENTER)
      infoContainer.add(timePanel, BorderLayout.SOUTH)

      panel.add(infoContainer, BorderLayout.CENTER)
      panel.add(controls, BorderLayout.SOUTH)
      panel

    /**
     * Configures all event handlers for UI components.
     *
     * @param queue
     *   Event queue for publishing events
     */
    private def setupEventHandlers(queue: Queue[IO, Event]): Unit =
      setupControlHandlers(queue)
      robotPanel.onSelectionChanged(() => updateRobotInfo())
      canvas.addSelectionListener(robotPanel.selectRobot)

    /**
     * Sets up event handlers for control buttons.
     *
     * @param queue
     *   Event queue for publishing control events
     */
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

      setupSpeedControl(queue)

    /**
     * Configures the speed control radio buttons.
     *
     * @param queue
     *   Event queue for speed change events
     */
    private def setupSpeedControl(queue: Queue[IO, Event]): Unit =
      controls.onSpeedChanged { speed =>
        val simulationSpeed = parseSpeed(speed)
        queue.offer(Event.TickSpeed(simulationSpeed)).unsafeRunAndForget()
      }

    /**
     * Converts speed label to SimulationSpeed enum.
     *
     * @param speed
     *   String representation of speed ("slow", "fast", or "normal")
     * @return
     *   Corresponding SimulationSpeed enum value
     */
    private def parseSpeed(speed: SpeedLabel): SimulationSpeed = speed match
      case "slow" => SimulationSpeed.SLOW
      case "fast" => SimulationSpeed.FAST
      case _ => SimulationSpeed.NORMAL

    /**
     * Handles start/stop button clicks.
     *
     * @param queue
     *   Event queue for simulation control events
     * @param isStart
     *   True if starting, false if stopping
     */
    private def handleSimulationControl(queue: Queue[IO, Event], isStart: Boolean): Unit =
      if isStart then startSimulation(queue)
      else showStopConfirmation(queue)

    /**
     * Starts the simulation and updates button states.
     *
     * @param queue
     *   Event queue for the resume event
     */
    private def startSimulation(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Resume).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = false)
      controls.pauseResumeButton.setEnabled(true)

    /**
     * Shows a confirmation dialog for stopping the simulation. Pauses the simulation while the dialog is shown.
     *
     * @param queue
     *   Event queue for pause/stop/resume events
     */
    private def showStopConfirmation(queue: Queue[IO, Event]): Unit =
      queue.offer(Event.Pause).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = true)

      val result = JOptionPane.showConfirmDialog(
        frame,
        UI.SimulationViewConstants.StopConfirmMessage,
        UI.SimulationViewConstants.StopConfirmTitle,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
      )

      if result == JOptionPane.YES_OPTION then
        queue.offer(Event.Stop).unsafeRunAndForget()
        controls.updateButtonText(isRunning = false, isPaused = false)
        controls.pauseResumeButton.setEnabled(false)
      else
        queue.offer(Event.Resume).unsafeRunAndForget()
        controls.updateButtonText(isRunning = true, isPaused = false)

    /**
     * Handles pause/resume button clicks.
     *
     * @param queue
     *   Event queue for pause/resume events
     * @param isPause
     *   True if pausing, false if resuming
     */
    private def handlePauseResume(queue: Queue[IO, Event], isPause: Boolean): Unit =
      val event = if isPause then Event.Pause else Event.Resume
      queue.offer(event).unsafeRunAndForget()
      controls.updateButtonText(isRunning = true, isPaused = isPause)

    /**
     * Updates the robot information panel with selected robot details.
     */
    private def updateRobotInfo(): Unit =
      val currentState = viewState.get
      val info = for
        selectedId <- robotPanel.selectedId
        robot <- currentState.robots.find(_.id.toString == selectedId)
        environment <- currentState.environment
      yield formatRobotInfo(robot, environment)

      robotPanel.setInfo(info.getOrElse(UI.SimulationViewConstants.DefaultRobotInfo))

    /**
     * Formats robot information for display.
     *
     * @param robot
     *   The robot whose information to format
     * @return
     *   Formatted string with robot details
     */
    private def formatRobotInfo(robot: Robot, environment: Environment): String =
      val readings = robot.senseAll[Id](environment)
      s"""Robot ID: ${robot.id.toString.take(UI.SimulationViewConstants.IdDisplayLength)}...
         |Position: (${s"%.${UI.SimulationViewConstants.PositionDecimals}f"
          .format(robot.position.x)}, ${s"%.${UI.SimulationViewConstants.PositionDecimals}f".format(robot.position.y)})
         |Orientation: ${s"%.${UI.SimulationViewConstants.OrientationDecimals}f"
          .format(robot.orientation.degrees)}°
         |Sensors:
         |  ${readings.prettyPrint.mkString("\n  ")}""".stripMargin
  end SimulationViewImpl

end SimulationView
