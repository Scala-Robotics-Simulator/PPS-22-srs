package io.github.srs.view.components.simulation

import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults
import io.github.srs.utils.SimulationDefaults.Canvas.*

/**
 * Canvas responsible for rendering the simulation environment and entities.
 */
class SimulationCanvas extends JPanel:

  import java.awt.{ BasicStroke, Color, Graphics, Graphics2D, RadialGradientPaint }

  /**
   * Internal state of the canvas that contains environment information, robots, selection state, cached static layers,
   * and sizing information.
   *
   * @param environment
   *   The current environment being displayed, if any
   * @param robots
   *   List of robots in the simulation
   * @param selectedId
   *   ID of the currently selected robot, if any
   * @param staticLayer
   *   Cached image of static elements (grid, labels, obstacles, lights)
   * @param lastSize
   *   Tuple of (envWidth, envHeight, canvasWidth, canvasHeight) used to detect size changes
   */
  private case class CanvasState(
      environment: Option[Environment] = None,
      robots: List[Robot] = Nil,
      selectedId: Option[String] = None,
      staticLayer: Option[BufferedImage] = None,
      lastSize: (Int, Int, Int, Int) = (-1, -1, -1, -1),
  )

  private val state = new AtomicReference(CanvasState())
  private val robotShape = new Ellipse2D.Double()
  private val gridStroke = new BasicStroke(gridStrokeWidth)

  /**
   * Updates the canvas state and triggers a repaint.
   *
   * @param env
   *   The environment to be displayed
   * @param robots
   *   The list of robots to render
   * @param selectedId
   *   The ID of the selected robot, if any
   */
  def update(env: Environment, robots: List[Robot], selectedId: Option[String]): Unit =
    state.updateAndGet(
      _.copy(
        environment = Some(env),
        robots = robots,
        selectedId = selectedId,
      ),
    )
    repaint()

  /**
   * Adds a listener that is invoked when a robot is clicked.
   *
   * @param onSelect
   *   Callback function that receives the ID of the selected robot
   */
  def addSelectionListener(onSelect: String => Unit): Unit =
    addMouseListener(
      new MouseAdapter():
        override def mouseClicked(e: MouseEvent): Unit =
          val currentState = state.get
          for
            env <- currentState.environment
            robot <- findRobotAt(e.getX, e.getY, env, currentState.robots)
          do onSelect(robot.id.toString),
    )

  /**
   * Finds a robot at the specified screen coordinates.
   *
   * @param x
   *   The x-coordinate in screen space
   * @param y
   *   The y-coordinate in screen space
   * @param env
   *   The current environment
   * @param robots
   *   The list of robots to search through
   * @return
   *   An Option containing the robot at the specified location, if any
   */
  private def findRobotAt(x: Int, y: Int, env: Environment, robots: List[Robot]): Option[Robot] =
    val worldX = x / scaleX(env)
    val worldY = y / scaleY(env)
    robots.find { r =>
      val dx = worldX - r.position.x
      val dy = worldY - r.position.y
      dx * dx + dy * dy <= r.shape.radius * r.shape.radius
    }

  /**
   * Calculates the scaling factor for the x-axis.
   *
   * @param env
   *   The current environment
   * @return
   *   The scaling factor from world to screen coordinates for the x-axis
   */
  private def scaleX(env: Environment): Double = getWidth.toDouble / env.width

  /**
   * Calculates the scaling factor for the y-axis.
   *
   * @param env
   *   The current environment
   * @return
   *   The scaling factor from world to screen coordinates for the y-axis
   */
  private def scaleY(env: Environment): Double = getHeight.toDouble / env.height

  /**
   * Overrides the paintComponent method to render the simulation.
   *
   * @param g
   *   The Graphics context to paint on
   */
  override def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val currentState = state.get
    for env <- currentState.environment do
      val updatedState = ensureStaticLayer(env, currentState)
      updatedState.staticLayer.foreach(g.drawImage(_, 0, 0, this))
      drawRobots(g, env, currentState.robots, currentState.selectedId)

  /**
   * Ensures that the static layer is up-to-date with the current environment and canvas size. If necessary, creates a
   * new static layer image.
   *
   * @param env
   *   The current environment
   * @param currentState
   *   The current canvas state
   * @return
   *   The updated canvas state
   */
  private def ensureStaticLayer(env: Environment, currentState: CanvasState): CanvasState =
    val size = (env.width, env.height, getWidth, getHeight)
    if currentState.staticLayer.isEmpty || currentState.lastSize != size then
      val img = createStaticLayerImage(env)
      val newState = currentState.copy(staticLayer = Some(img), lastSize = size)
      state.set(newState)
      newState
    else currentState

  /**
   * Creates a new image for the static layer (grid, labels, static entities).
   *
   * @param env
   *   The environment to render in the static layer
   * @return
   *   A BufferedImage containing the static elements
   */
  private def createStaticLayerImage(env: Environment): BufferedImage =
    val img = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    val g2 = img.createGraphics()

    drawGrid(g2, env)
    drawLabels(g2, env)
    drawStaticEntities(g2, env)

    g2.dispose()
    img

  /**
   * Draws the grid on the given graphics context.
   *
   * @param g2
   *   The graphics context to draw on
   * @param env
   *   The environment defining the grid size
   */
  private def drawGrid(g2: Graphics2D, env: Environment): Unit =
    g2.setColor(Color.LIGHT_GRAY)
    g2.setStroke(gridStroke)

    val sX = scaleX(env)
    val sY = scaleY(env)

    (0 to env.width).foreach { x =>
      val px = (x * sX).toInt
      g2.drawLine(px, 0, px, getHeight)
    }

    (0 to env.height).foreach { y =>
      val py = (y * sY).toInt
      g2.drawLine(0, py, getWidth, py)
    }

  /**
   * Draws coordinate labels on the grid.
   *
   * @param g2
   *   The graphics context to draw on
   * @param env
   *   The environment defining the grid size
   */
  private def drawLabels(g2: Graphics2D, env: Environment): Unit =
    g2.setColor(Color.DARK_GRAY)
    val bottom = getHeight - labelBottomOffset
    val sX = scaleX(env)
    val sY = scaleY(env)
    val stepX = adaptiveLabelStep(sX)
    val stepY = adaptiveLabelStep(sY)

    (0 to env.width by stepX).foreach { x =>
      val px = (x * sX).toInt
      g2.drawString(x.toString, px + labelXOffset, bottom)
    }

    (0 to env.height by stepY).foreach { y =>
      val py = (y * sY).toInt
      g2.drawString(
        y.toString,
        labelXOffset,
        Math.min(bottom, py + labelYOffset),
      )
    }

  end drawLabels

  /**
   * Draws all static entities in the environment.
   *
   * @param g2
   *   The graphics context to draw on
   * @param env
   *   The environment containing the static entities
   */
  private def drawStaticEntities(g2: Graphics2D, env: Environment): Unit =
    val sX = scaleX(env)
    val sY = scaleY(env)

    env.entities.foreach:
      case StaticEntity.Obstacle(_, pos, _, w, h) =>
        drawObstacle(g2, pos, w, h, sX, sY)
      case StaticEntity.Light(_, pos, _, radius, _, _, _) =>
        drawLight(g2, pos, radius, sX, sY)
      case _ => ()

  /**
   * Draws an obstacle entity with a gradient fill.
   *
   * @param g2
   *   The graphics context to draw on
   * @param pos
   *   The position of the obstacle
   * @param w
   *   The width of the obstacle
   * @param h
   *   The height of the obstacle
   * @param sX
   *   The x-axis scaling factor
   * @param sY
   *   The y-axis scaling factor
   */
  private def drawObstacle(g2: Graphics2D, pos: Point2D, w: Double, h: Double, sX: Double, sY: Double): Unit =
    val x = (pos.x - w / 2) * sX
    val y = (pos.y - h / 2) * sY
    val width = (w * sX).toInt
    val height = (h * sY).toInt

    val gradient = new java.awt.GradientPaint(
      x.toFloat,
      y.toFloat,
      new Color(120, 120, 120),
      x.toFloat + width,
      y.toFloat + height,
      new Color(80, 80, 80),
    )
    g2.setPaint(gradient)
    g2.fillRect(x.toInt, y.toInt, width, height)

    g2.setColor(new Color(60, 60, 60))
    g2.setStroke(new BasicStroke(1.5f))
    g2.drawRect(x.toInt, y.toInt, width, height)

  end drawObstacle

  /**
   * Draws a light entity with a radial gradient effect.
   *
   * @param g2
   *   The graphics context to draw on
   * @param pos
   *   The position of the light
   * @param radius
   *   The radius of the light's effect
   * @param sX
   *   The x-axis scaling factor
   * @param sY
   *   The y-axis scaling factor
   */
  private def drawLight(g2: Graphics2D, pos: Point2D, radius: Double, sX: Double, sY: Double): Unit =
    val cx = (pos.x * sX).toInt
    val cy = (pos.y * sY).toInt
    val diameter = (4 * radius * Math.min(sX, sY)).toInt
    val size = Math.max(minLightSize, diameter)
    val x = cx - size / 2
    val y = cy - size / 2

    val paint = new RadialGradientPaint(
      new java.awt.geom.Point2D.Double(cx, cy),
      (size / 2).toFloat,
      Array(0f, 1f),
      Array(new Color(255, 255, 200, 200), new Color(255, 140, 0, 80)),
    )

    g2.setPaint(paint)
    g2.fill(new Ellipse2D.Double(x, y, size, size))
    g2.setColor(Color.ORANGE)
    g2.setStroke(new BasicStroke(lightStroke))
    g2.drawOval(x, y, size, size)

  end drawLight

  /**
   * Calculates an appropriate step size for grid labels based on the current scale.
   *
   * @param scale
   *   The current scale factor
   * @return
   *   An integer step size that ensures labels are appropriately spaced
   */
  private def adaptiveLabelStep(scale: Double): Int =
    val desiredPx = labelDesiredPx
    val raw = desiredPx / scale
    val steps = List(1, 2, 5)

    @annotation.tailrec
    def findStep(multiplier: Int): Int =
      steps.find(_ * multiplier >= raw) match
        case Some(step) => step * multiplier
        case None => findStep(multiplier * 10)

    findStep(1)

  /**
   * Draws all robots in the environment.
   *
   * @param gr
   *   The graphics context to draw on
   * @param env
   *   The environment containing the robots
   * @param robots
   *   The list of robots to draw
   * @param selectedId
   *   The ID of the selected robot, if any
   */
  private def drawRobots(gr: Graphics, env: Environment, robots: List[Robot], selectedId: Option[String]): Unit =
    gr match
      case g2: Graphics2D =>
        val sX = scaleX(env)
        val sY = scaleY(env)
        robots.foreach(drawRobot(g2, _, sX, sY, selectedId))
      case _ => ()

  /**
   * Draws a single robot.
   *
   * @param g2
   *   The graphics context to draw on
   * @param robot
   *   The robot to draw
   * @param sX
   *   The x-axis scaling factor
   * @param sY
   *   The y-axis scaling factor
   * @param selectedId
   *   The ID of the selected robot, if any
   */
  private def drawRobot(g2: Graphics2D, robot: Robot, sX: Double, sY: Double, selectedId: Option[String]): Unit =
    robot.shape match
      case ShapeType.Circle(radius) =>
        drawRobotBody(g2, robot, radius, sX, sY, selectedId)
        drawRobotDirection(g2, robot, radius, sX, sY)

  /**
   * Draws the body of a robot with a radial gradient and selection highlight if selected.
   *
   * @param g2
   *   The graphics context to draw on
   * @param robot
   *   The robot to draw
   * @param radius
   *   The radius of the robot
   * @param sX
   *   The x-axis scaling factor
   * @param sY
   *   The y-axis scaling factor
   * @param selectedId
   *   The ID of the selected robot, if any
   */
  private def drawRobotBody(
      g2: Graphics2D,
      robot: Robot,
      radius: Double,
      sX: Double,
      sY: Double,
      selectedId: Option[String],
  ): Unit =
    val x = (robot.position.x - radius) * sX
    val y = (robot.position.y - radius) * sY
    val diamX = 2 * radius * sX
    val diamY = 2 * radius * sY

    robotShape.setFrame(x, y, diamX, diamY)

    val cx = (robot.position.x * sX).toFloat
    val cy = (robot.position.y * sY).toFloat
    val isSelected = selectedId.contains(robot.id.toString)

    val gradient = new RadialGradientPaint(
      cx,
      cy,
      (radius * Math.min(sX, sY)).toFloat,
      Array(0f, 1f),
      if isSelected then Array(new Color(255, 100, 100), new Color(200, 50, 50))
      else Array(new Color(100, 150, 255), new Color(50, 100, 200)),
    )

    g2.setPaint(gradient)
    g2.fill(robotShape)

    g2.setColor(new Color(0, 0, 0, 50))
    g2.setStroke(new BasicStroke(3f))
    g2.draw(robotShape)

    g2.setColor(if isSelected then new Color(150, 0, 0) else new Color(0, 50, 150))
    g2.setStroke(
      new BasicStroke(
        if isSelected then SimulationDefaults.DynamicEntity.Robot.selectionStroke
        else SimulationDefaults.DynamicEntity.Robot.normalStroke,
      ),
    )
    g2.draw(robotShape)

  end drawRobotBody

  /**
   * Draws a directional arrow indicating the robot's orientation.
   *
   * @param g2
   *   The graphics context to draw on
   * @param robot
   *   The robot whose direction to indicate
   * @param radius
   *   The radius of the robot
   * @param sX
   *   The x-axis scaling factor
   * @param sY
   *   The y-axis scaling factor
   */
  private def drawRobotDirection(g2: Graphics2D, robot: Robot, radius: Double, sX: Double, sY: Double): Unit =
    import SimulationDefaults.DynamicEntity.Robot.{ arrowLengthFactor, arrowWidthFactor, minArrowWidth }
    def scaled(v: Double, scale: Double): Int = (v * scale).toInt
    def point(x: Double, y: Double): (Int, Int) = (x.toInt, y.toInt)

    val cx = scaled(robot.position.x, sX)
    val cy = scaled(robot.position.y, sY)

    val angle = robot.orientation.toRadians
    val minScale = math.min(sX, sY)
    val length = radius * arrowLengthFactor * minScale
    val width = math.max(minArrowWidth.toDouble, radius * minScale * arrowWidthFactor)
    val halfWidth = width / 2

    val cosA = math.cos(angle)
    val sinA = math.sin(angle)

    val tip = point(cx + cosA * length, cy + sinA * length)
    val baseLeft = point(cx - sinA * halfWidth, cy + cosA * halfWidth)
    val baseRight = point(cx + sinA * halfWidth, cy - cosA * halfWidth)

    val (tipX, tipY) = tip
    val (baseLeftX, baseLeftY) = baseLeft
    val (baseRightX, baseRightY) = baseRight

    val arrow = new java.awt.Polygon(
      Array(tipX, baseLeftX, baseRightX),
      Array(tipY, baseLeftY, baseRightY),
      3,
    )

    g2.fill(arrow)
    g2.setColor(Color.BLACK)
    g2.fillPolygon(arrow)
  end drawRobotDirection

end SimulationCanvas
