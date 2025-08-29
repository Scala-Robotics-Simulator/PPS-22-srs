package io.github.srs.view.components.simulation

import java.awt.*
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel

import scala.collection.immutable.List

import io.github.srs.model.entity.Point2D.*
import io.github.srs.model.entity.dynamicentity.Robot
import io.github.srs.model.entity.staticentity.StaticEntity
import io.github.srs.model.entity.{ Point2D, ShapeType }
import io.github.srs.model.environment.Environment
import io.github.srs.utils.SimulationDefaults
import io.github.srs.utils.SimulationDefaults.Canvas.*
import io.github.srs.view.state.SimulationViewState
import io.github.srs.utils.SimulationDefaults.DynamicEntity.Sensor.ProximitySensor.DefaultRange
import io.github.srs.model.entity.dynamicentity.sensor.Sensor.senseAll
import io.github.srs.model.entity.dynamicentity.sensor.SensorReadings.proximityReadings
import cats.Id

/**
 * Canvas component responsible for rendering the simulation environment. Supports static layer caching for improved
 * performance.
 *
 * @param insideConfiguration
 *   If true, the static layer is recreated on every paint and robot sensor lines are not drawn (for configuration
 *   preview)
 */
class SimulationCanvas(private val insideConfiguration: Boolean = false) extends JPanel:

  /**
   * Viewport configuration for world-to-screen transformation.
   *
   * @param scale
   *   Scaling factor from world-to-screen coordinates
   * @param offsetX
   *   Horizontal offset for centering
   * @param offsetY
   *   Vertical offset for centering
   * @param width
   *   Scaled width of the environment
   * @param height
   *   Scaled height of the environment
   */
  private case class Viewport(scale: Double, offsetX: Int, offsetY: Int, width: Int, height: Int)

  private val state = new AtomicReference(SimulationViewState())
  private val robotShape = new Ellipse2D.Double()
  private val gridStroke = new BasicStroke(GridStrokeWidth)

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
        case g2: Graphics2D => drawRobots(g2, env)
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
    val scale = math.min(getWidth.toDouble / env.width, getHeight.toDouble / env.height)
    val width = (env.width * scale).toInt
    val height = (env.height * scale).toInt
    val offsetX = (getWidth - width) / 2
    val offsetY = (getHeight - height) / 2
    Viewport(scale, offsetX, offsetY, width, height)

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
    val g2 = img.createGraphics()
    val vp = viewport(env)

    g2.setColor(getBackground)
    g2.fillRect(0, 0, getWidth, getHeight)

    drawGrid(g2, env, vp)
    drawLabels(g2, env, vp)
    drawStaticEntities(g2, env, vp)

    g2.dispose()
    img

  /**
   * Draws the coordinate grid.
   *
   * @param g2
   *   Graphics context
   * @param env
   *   Environment for dimensions
   * @param vp
   *   Viewport configuration
   */
  private def drawGrid(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    g2.setColor(Color.LIGHT_GRAY)
    g2.setStroke(gridStroke)

    (0 to env.width).foreach { x =>
      val sx = vp.offsetX + (x * vp.scale).toInt
      g2.drawLine(sx, vp.offsetY, sx, vp.offsetY + vp.height)
    }
    (0 to env.height).foreach { y =>
      val sy = vp.offsetY + (y * vp.scale).toInt
      g2.drawLine(vp.offsetX, sy, vp.offsetX + vp.width, sy)
    }

  /**
   * Draws coordinate labels with adaptive stepping.
   *
   * @param g2
   *   Graphics context
   * @param env
   *   Environment for dimensions
   * @param vp
   *   Viewport configuration
   */
  private def drawLabels(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    g2.setColor(Color.DARK_GRAY)
    val bottom = vp.offsetY + vp.height - LabelBottomOffset

    val step = adaptiveLabelStep(vp.scale)

    (0 to env.width by step).foreach { x =>
      g2.drawString(x.toString, vp.offsetX + (x * vp.scale).toInt + LabelXOffset, bottom)
    }
    (0 to env.height by step).foreach { y =>
      val py = vp.offsetY + (y * vp.scale).toInt
      g2.drawString(y.toString, vp.offsetX + LabelXOffset, Math.min(bottom, py + LabelYOffset))
    }

  /**
   * Calculates adaptive step size for labels based on zoom level.
   *
   * @param scale
   *   Current zoom scale
   * @return
   *   Step size for label spacing
   */
  private def adaptiveLabelStep(scale: Double): Int =
    val raw = LabelDesiredPx / scale
    val steps = List(1, 2, 5)
    LazyList
      .iterate(1)(_ * 10)
      .flatMap(m => steps.map(_ * m))
      .find(_ >= raw)
      .getOrElse(1)

  /**
   * Draws all static entities (obstacles and lights).
   *
   * @param g2
   *   Graphics context
   * @param env
   *   Environment containing entities
   * @param vp
   *   Viewport configuration
   */
  private def drawStaticEntities(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    env.entities.foreach:
      case StaticEntity.Obstacle(_, pos, orientation, w, h) =>
        drawObstacle(g2, pos, orientation.degrees, w, h, vp)
      case StaticEntity.Light(_, pos, _, radius, _, _, _) =>
        drawLight(g2, pos, radius, vp)
      case _ => ()

  /**
   * Draws an obstacle with gradient fill.
   *
   * @param g2
   *   Graphics context
   * @param pos
   *   Position in world coordinates
   * @param orientation
   *   Rotation in degrees
   * @param w
   *   Width of an obstacle
   * @param h
   *   Height of an obstacle
   * @param vp
   *   Viewport configuration
   */
  private def drawObstacle(
      g2: Graphics2D,
      pos: Point2D,
      orientation: Double,
      w: Double,
      h: Double,
      vp: Viewport,
  ): Unit =
    import io.github.srs.utils.SimulationDefaults.UI.{ Colors, Strokes }

    val savedTransform = g2.getTransform
    val centerX = vp.offsetX + pos.x * vp.scale
    val centerY = vp.offsetY + pos.y * vp.scale
    g2.rotate(orientation.toRadians, centerX, centerY)

    val x = (vp.offsetX + (pos.x - w / 2) * vp.scale).toInt
    val y = (vp.offsetY + (pos.y - h / 2) * vp.scale).toInt
    val width = (w * vp.scale).toInt
    val height = (h * vp.scale).toInt

    val gradient = new GradientPaint(
      x.toFloat,
      y.toFloat,
      Colors.obstacleGradientStart,
      (x + width).toFloat,
      (y + height).toFloat,
      Colors.obstacleGradientEnd,
    )

    g2.setPaint(gradient)
    g2.fillRect(x, y, width, height)

    g2.setColor(Colors.obstacleBorder)
    g2.setStroke(new BasicStroke(Strokes.ObstacleStroke))
    g2.drawRect(x, y, width, height)

    g2.setTransform(savedTransform)

  end drawObstacle

  /**
   * Draws a light source with radial gradient.
   *
   * @param g2
   *   Graphics context
   * @param pos
   *   Position in world coordinates
   * @param radius
   *   Effective radius of the light
   * @param vp
   *   Viewport configuration
   */
  private def drawLight(g2: Graphics2D, pos: Point2D, radius: Double, vp: Viewport): Unit =
    import io.github.srs.utils.SimulationDefaults.UI.Colors

    val cx = (vp.offsetX + pos.x * vp.scale).toInt
    val cy = (vp.offsetY + pos.y * vp.scale).toInt
    val size = Math.max(MinLightSize, (4 * radius * vp.scale).toInt)
    val x = cx - size / 2
    val y = cy - size / 2

    val paint = new RadialGradientPaint(
      new java.awt.geom.Point2D.Double(cx, cy),
      (size / 2).toFloat,
      Array(0f, 1f),
      Array(Colors.lightCenter, Colors.lightEdge),
    )

    g2.setPaint(paint)
    g2.fill(new Ellipse2D.Double(x, y, size, size))
    g2.setColor(Color.ORANGE)
    g2.setStroke(new BasicStroke(LightStroke))
    g2.drawOval(x, y, size, size)

  end drawLight

  /**
   * Draws all robots in the environment.
   *
   * @param g2
   *   Graphics context
   * @param env
   *   Environment containing robots
   */
  private def drawRobots(g2: Graphics2D, env: Environment): Unit =
    import io.github.srs.model.environment.robots
    val vp = viewport(env)
    val currentState = state.get
    env.robots.foreach(drawRobot(g2, _, env, vp, currentState.selectedRobotId))

  /**
   * Draws a single robot with body and direction indicator.
   *
   * @param g2
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param vp
   *   Viewport configuration
   * @param selectedId
   *   Optional ID of the selected robot
   */
  private def drawRobot(
      g2: Graphics2D,
      robot: Robot,
      env: Environment,
      vp: Viewport,
      selectedId: Option[String],
  ): Unit =
    robot.shape match
      case ShapeType.Circle(radius) =>
        val isSelected = selectedId.contains(robot.id.toString)
        drawRobotBody(g2, robot, radius, vp, isSelected)
        drawRobotDirection(g2, robot, radius, env, vp)

  /**
   * Draws the robot's circular body with gradient and border.
   *
   * @param g2
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param radius
   *   Radius of the robot
   * @param vp
   *   Viewport configuration
   * @param isSelected
   *   True if this robot is currently selected
   */
  private def drawRobotBody(g2: Graphics2D, robot: Robot, radius: Double, vp: Viewport, isSelected: Boolean): Unit =
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.{ NormalStroke, SelectionStroke }
    import io.github.srs.utils.SimulationDefaults.UI.{ Colors, Strokes }

    val x = vp.offsetX + (robot.position.x - radius) * vp.scale
    val y = vp.offsetY + (robot.position.y - radius) * vp.scale
    val d = 2 * radius * vp.scale

    robotShape.setFrame(x, y, d, d)

    val colors =
      if isSelected then (Colors.robotSelected, Colors.robotSelectedDark, Colors.robotSelectedBorder)
      else (Colors.robotDefault, Colors.robotDefaultDark, Colors.robotDefaultBorder)

    val gradient = new RadialGradientPaint(
      (vp.offsetX + robot.position.x * vp.scale).toFloat,
      (vp.offsetY + robot.position.y * vp.scale).toFloat,
      (radius * vp.scale).toFloat,
      Array(0f, 1f),
      Array(colors._1, colors._2),
    )

    g2.setPaint(gradient)
    g2.fill(robotShape)

    g2.setColor(Colors.robotShadow)
    g2.setStroke(new BasicStroke(Strokes.RobotShadowStroke))
    g2.draw(robotShape)

    g2.setColor(colors._3)
    val strokeWidth = if isSelected then SelectionStroke else NormalStroke
    g2.setStroke(new BasicStroke(strokeWidth))
    g2.draw(robotShape)

  end drawRobotBody

  /**
   * Draws an arrow indicating the robot's orientation.
   *
   * @param g2
   *   Graphics context
   * @param robot
   *   The robot to draw
   * @param radius
   *   Radius of the robot
   * @param vp
   *   Viewport configuration
   */
  private def drawRobotDirection(g2: Graphics2D, robot: Robot, radius: Double, env: Environment, vp: Viewport): Unit =
    import SimulationDefaults.DynamicEntity.Robot.*

    val cx = (vp.offsetX + robot.position.x * vp.scale).toInt
    val cy = (vp.offsetY + robot.position.y * vp.scale).toInt
    val angle = robot.orientation.toRadians
    val length = radius * ArrowLengthFactor * vp.scale
    val width = math.max(MinArrowWidth.toDouble, radius * vp.scale * ArrowWidthFactor)

    val arrow = createArrowPolygon(cx, cy, angle, length, width)
    g2.setColor(Color.BLACK)
    g2.fillPolygon(arrow)
    if !insideConfiguration then drawSensorLines(g2, robot, radius, env, vp)

  private def drawSensorLines(g2: Graphics2D, robot: Robot, radius: Double, env: Environment, vp: Viewport): Unit =
    val cx = (vp.offsetX + robot.position.x * vp.scale).toInt
    val cy = (vp.offsetY + robot.position.y * vp.scale).toInt
    val scaledRadius = radius * vp.scale

    val readings = robot.senseAll[Id](env).proximityReadings

    // Assuming robot has a collection of sensors with orientations
    readings.foreach { reading =>
      val sensor = reading.sensor
      val value = reading.value
      val sensorAngle = sensor.offset.toRadians + robot.orientation.toRadians

      // Calculate start point (on robot border)
      val startX = cx + (scaledRadius * math.cos(sensorAngle)).toInt
      val startY = cy + (scaledRadius * math.sin(sensorAngle)).toInt

      // Calculate end point (sensor range)
      val sensorLength = DefaultRange * vp.scale // Assuming sensors have a range property
      val endX = startX + (sensorLength * math.cos(sensorAngle) * value).toInt
      val endY = startY + (sensorLength * math.sin(sensorAngle) * value).toInt

      // Draw sensor line
      g2.setColor(Color.BLUE) // Or any color you prefer for sensors
      g2.setStroke(new BasicStroke(1.0f)) // Thin line for sensors
      g2.drawLine(startX, startY, endX, endY)

      // Optional: Draw a small circle at the end to show sensor detection point
      val dotSize = 3
      g2.fillOval(endX - dotSize / 2, endY - dotSize / 2, dotSize, dotSize)
    }

  end drawSensorLines

  /**
   * Creates a triangular polygon representing an arrow.
   *
   * @param cx
   *   Center X coordinate
   * @param cy
   *   Center Y coordinate
   * @param angle
   *   Direction angle in radians
   * @param length
   *   Length of the arrow
   * @param width
   *   Width of the arrow base
   * @return
   *   Polygon representing the arrow
   */
  private def createArrowPolygon(cx: Int, cy: Int, angle: Double, length: Double, width: Double): Polygon =
    val (cosA, sinA) = (math.cos(angle), math.sin(angle))
    val halfWidth = width / 2

    new Polygon(
      Array(
        (cx + cosA * length).toInt,
        (cx - sinA * halfWidth).toInt,
        (cx + sinA * halfWidth).toInt,
      ),
      Array(
        (cy + sinA * length).toInt,
        (cy + cosA * halfWidth).toInt,
        (cy - cosA * halfWidth).toInt,
      ),
      3,
    )
end SimulationCanvas
