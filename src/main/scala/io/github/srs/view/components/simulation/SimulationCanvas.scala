package io.github.srs.view.components.simulation

import java.awt.*
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.ShapeType
import io.github.srs.model.environment.Environment
import io.github.srs.view.rendering.{ EnvironmentDrawing, Viewport }
import io.github.srs.view.state.SimulationViewState
import io.github.srs.model.entity.dynamicentity.robot.Robot

/**
 * Canvas component responsible for rendering the simulation environment. Supports static layer caching for improved
 * performance.
 *
 * @param insideConfiguration
 *   If true, the static layer is recreated on every paint and robot sensor lines are not drawn (for configuration
 *   preview)
 */
class SimulationCanvas(private val insideConfiguration: Boolean = false) extends JPanel with EnvironmentDrawing:

  private val state = new AtomicReference(SimulationViewState())

  /**
   * Updates the canvas with new environment data.
   *
   * @param env
   *   The environment to render
   * @param selectedId
   *   Optional ID of the selected robot
   */
  def update(env: Environment, selectedId: Option[String]): Unit =
    state.updateAndGet(_.withEnvironment(env).withSelection(selectedId))
    repaint()

  /**
   * Adds a mouse-click listener for robot selection.
   *
   * @param onSelect
   *   Callback function called with robot ID when clicked
   */
  def addSelectionListener(onSelect: String => Unit): Unit =
    addMouseListener(
      new MouseAdapter:
        override def mouseClicked(e: MouseEvent): Unit =
          findRobotAt(e.getX, e.getY).foreach(r => onSelect(r.id.toString)),
    )

  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val currentState = state.get

    currentState.environment.foreach { env =>
      ensureStaticLayer(env)
      state.get.staticLayer.foreach(g.drawImage(_, 0, 0, this))
      g match
        case g2: Graphics2D => drawRobots(g2, env, viewport(env), currentState.selectedRobotId)
        case _ => ()
    }

  /**
   * Calculates viewport transformation for centering and scaling.
   *
   * @param env
   *   The environment to fit in the viewport
   * @return
   *   Viewport configuration
   */
  private def viewport(env: Environment): Viewport =
    calculateViewport(env, getWidth, getHeight)

  /**
   * Finds a robot at the given screen coordinates.
   *
   * @param x
   *   Screen X coordinate
   * @param y
   *   Screen Y coordinate
   * @return
   *   Option containing the robot at that position, if any
   */
  private def findRobotAt(x: Int, y: Int): Option[Robot] =
    import io.github.srs.model.environment.robots
    val currentState = state.get
    for
      env <- currentState.environment
      vp = viewport(env)
      if x >= vp.offsetX && x <= vp.offsetX + vp.width && y >= vp.offsetY && y <= vp.offsetY + vp.height
      worldX = (x - vp.offsetX) / vp.scale
      worldY = (y - vp.offsetY) / vp.scale
      robot <- env.robots.find { r =>
        val dx = worldX - r.position.x
        val dy = worldY - r.position.y
        dx * dx + dy * dy <= r.shape.radius * r.shape.radius
      }
    yield robot

  /**
   * Ensures the static layer is up to date with the current environment.
   *
   * @param env
   *   The environment to render
   */
  private def ensureStaticLayer(env: Environment): Unit =
    val size = (env.width, env.height, getWidth, getHeight)
    if insideConfiguration || state.get.needsStaticLayerUpdate(size) then
      val img = createStaticLayerImage(env)
      state.updateAndGet(_.withStaticLayer(img, size)): Unit

  /**
   * Creates a buffered image containing static elements (grid, obstacles, lights).
   *
   * @param env
   *   The environment to render
   * @return
   *   BufferedImage with static elements
   */
  private def createStaticLayerImage(env: Environment): BufferedImage =
    val img = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()
    val vp = viewport(env)

    g.setColor(getBackground)
    g.fillRect(0, 0, getWidth, getHeight)

    drawGrid(g, env, vp)
    drawLabels(g, env, vp)
    drawStaticEntities(g, env, vp)

    g.dispose()
    img

  /**
   * Draws a single robot with body and direction indicator.
   *
   * @param g
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param vp
   *   Viewport configuration
   * @param selectedId
   *   Optional ID of the selected robot
   */
  override protected def drawRobot(
      g: Graphics2D,
      robot: Robot,
      env: Environment,
      vp: Viewport,
      selectedId: Option[String],
  ): Unit =
    robot.shape match
      case ShapeType.Circle(radius) =>
        val isSelected = selectedId.contains(robot.id.toString)
        drawRobotBody(g, robot, radius, vp, isSelected)
        drawRobotDirection(g, robot, radius, vp)
        if !insideConfiguration then drawSensorLines(g, robot, radius, env, vp)

end SimulationCanvas
