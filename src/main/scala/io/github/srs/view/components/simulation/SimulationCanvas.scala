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
import io.github.srs.view.state.SimulationState

/**
 * Canvas responsible for rendering the simulation environment and entities. This version preserves aspect ratio (no
 * stretching) using a uniform scale + letterboxing.
 */
class SimulationCanvas extends JPanel:

  import java.awt.{ BasicStroke, Graphics, Graphics2D, RadialGradientPaint }

  // ---- Viewport (uniform scale + offsets) -----------------------------------

  private case class Viewport(s: Double, ox: Int, oy: Int, dw: Int, dh: Int)

  /** Compute a uniform scale that fits the world in the panel and center it. */
  private def viewport(env: Environment): Viewport =
    val s = math.min(getWidth.toDouble / env.width, getHeight.toDouble / env.height)
    val dw = (env.width * s).toInt
    val dh = (env.height * s).toInt
    val ox = (getWidth - dw) / 2
    val oy = (getHeight - dh) / 2
    Viewport(s, ox, oy, dw, dh)

  // ---- State & strokes ------------------------------------------------------

  private val state = new AtomicReference(SimulationState())
  private val robotShape = new Ellipse2D.Double()
  private val gridStroke = new BasicStroke(gridStrokeWidth)

  // ---- Public API -----------------------------------------------------------

  /**
   * Updates the canvas state and triggers a repaint.
   *
   * @param env
   *   The environment to be displayed
   * @param selectedId
   *   The ID of the selected robot, if any
   */
  def update(env: Environment, selectedId: Option[String]): Unit =
    state.updateAndGet(_.withEnvironment(env).withSelection(selectedId))
    repaint()

  /**
   * Adds a listener that is invoked when a robot is clicked.
   */
  def addSelectionListener(onSelect: String => Unit): Unit =
    addMouseListener(
      new MouseAdapter():
        override def mouseClicked(e: MouseEvent): Unit =
          findRobotAt(e.getX, e.getY).foreach(r => onSelect(r.id.toString)),
    )

  // ---- Input / hit testing --------------------------------------------------

  /**
   * Finds a robot at the specified screen coordinates using the current environment.
   */
  private def findRobotAt(x: Int, y: Int): Option[Robot] =
    import io.github.srs.model.environment.robots
    val currentState = state.get
    for
      env <- currentState.environment
      vp = viewport(env)
      if x >= vp.ox && x <= vp.ox + vp.dw && y >= vp.oy && y <= vp.oy + vp.dh
      worldX = (x - vp.ox) / vp.s
      worldY = (y - vp.oy) / vp.s
      robot <- env.robots.find { r =>
        val dx = worldX - r.position.x
        val dy = worldY - r.position.y
        dx * dx + dy * dy <= r.shape.radius * r.shape.radius
      }
    yield robot

  // ---- Painting -------------------------------------------------------------

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
   * Ensures that the static layer is up-to-date with the current environment and canvas size.
   */
  private def ensureStaticLayer(env: Environment): Unit =
    val size = (env.width, env.height, getWidth, getHeight)
    if state.get.needsStaticLayerUpdate(size) then
      val img = createStaticLayerImage(env)
      state.updateAndGet(_.withStaticLayer(img, size)): Unit

  /**
   * Creates a new image for the static layer (grid, labels, static entities).
   */
  private def createStaticLayerImage(env: Environment): BufferedImage =
    val img = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    val g2 = img.createGraphics()
    val vp = viewport(env)

    // letterbox background
    g2.setColor(getBackground)
    g2.fillRect(0, 0, getWidth, getHeight)

    drawGrid(g2, env, vp)
    drawLabels(g2, env, vp)
    drawStaticEntities(g2, env, vp)

    g2.dispose()
    img

  // ---- Grid & labels --------------------------------------------------------

  private def drawGrid(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    g2.setColor(java.awt.Color.LIGHT_GRAY)
    g2.setStroke(gridStroke)

    (0 to env.width).foreach { x =>
      val sx = vp.ox + (x * vp.s).toInt
      g2.drawLine(sx, vp.oy, sx, vp.oy + vp.dh)
    }
    (0 to env.height).foreach { y =>
      val sy = vp.oy + (y * vp.s).toInt
      g2.drawLine(vp.ox, sy, vp.ox + vp.dw, sy)
    }

  private def drawLabels(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    g2.setColor(java.awt.Color.DARK_GRAY)
    val bottom = vp.oy + vp.dh - labelBottomOffset

    val stepX = adaptiveLabelStep(vp.s)
    val stepY = adaptiveLabelStep(vp.s)

    (0 to env.width by stepX).foreach { x =>
      g2.drawString(x.toString, vp.ox + (x * vp.s).toInt + labelXOffset, bottom)
    }
    (0 to env.height by stepY).foreach { y =>
      val py = vp.oy + (y * vp.s).toInt
      g2.drawString(y.toString, vp.ox + labelXOffset, Math.min(bottom, py + labelYOffset))
    }

  /**
   * Calculates an adaptive step size for grid labels based on the current scale to maintain readability.
   */
  private def adaptiveLabelStep(scale: Double): Int =
    val raw = labelDesiredPx / scale
    val steps = List(1, 2, 5)
    LazyList
      .iterate(1)(_ * 10)
      .flatMap(m => steps.map(_ * m))
      .find(_ >= raw)
      .getOrElse(1)

  // ---- Static entities ------------------------------------------------------

  private def drawStaticEntities(g2: Graphics2D, env: Environment, vp: Viewport): Unit =
    env.entities.foreach:
      case StaticEntity.Obstacle(_, pos, orientation, w, h) =>
        drawObstacle(g2, pos, orientation.degrees, w, h, vp)
      case StaticEntity.Light(_, pos, _, radius, _, _, _) =>
        drawLight(g2, pos, radius, vp)
      case _ => ()

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

    // rotate around obstacle center (screen space)
    val centerX = vp.ox + pos.x * vp.s
    val centerY = vp.oy + pos.y * vp.s
    g2.rotate(orientation.toRadians, centerX, centerY)

    val rect = calculateRect(pos, w, h, vp)

    val gradient = createGradient(rect, Colors.obstacleGradientStart, Colors.obstacleGradientEnd)

    g2.setPaint(gradient)
    g2.fillRect(rect._1, rect._2, rect._3, rect._4)

    g2.setColor(Colors.obstacleBorder)
    g2.setStroke(new BasicStroke(Strokes.obstacleStroke))
    g2.drawRect(rect._1, rect._2, rect._3, rect._4)

    g2.setTransform(savedTransform)

  end drawObstacle

  private def calculateRect(pos: Point2D, w: Double, h: Double, vp: Viewport): (Int, Int, Int, Int) =
    val x = (vp.ox + (pos.x - w / 2) * vp.s).toInt
    val y = (vp.oy + (pos.y - h / 2) * vp.s).toInt
    val width = (w * vp.s).toInt
    val height = (h * vp.s).toInt
    (x, y, width, height)

  private def createGradient(
      rect: (Int, Int, Int, Int),
      c1: java.awt.Color,
      c2: java.awt.Color,
  ): java.awt.GradientPaint =
    new java.awt.GradientPaint(
      rect._1.toFloat,
      rect._2.toFloat,
      c1,
      (rect._1 + rect._3).toFloat,
      (rect._2 + rect._4).toFloat,
      c2,
    )

  private def drawLight(g2: Graphics2D, pos: Point2D, radius: Double, vp: Viewport): Unit =
    import io.github.srs.utils.SimulationDefaults.Canvas.{ lightStroke, minLightSize }
    import io.github.srs.utils.SimulationDefaults.UI.Colors

    val cx = (vp.ox + pos.x * vp.s).toInt
    val cy = (vp.oy + pos.y * vp.s).toInt
    val size = Math.max(minLightSize, (4 * radius * vp.s).toInt)
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
    g2.setColor(java.awt.Color.ORANGE)
    g2.setStroke(new BasicStroke(lightStroke))
    g2.drawOval(x, y, size, size)
  end drawLight

  // ---- Robots ---------------------------------------------------------------

  private def drawRobots(g2: Graphics2D, env: Environment): Unit =
    import io.github.srs.model.environment.robots
    val vp = viewport(env)
    val currentState = state.get
    env.robots.foreach(drawRobot(g2, _, vp, currentState.selectedRobotId))

  private def drawRobot(g2: Graphics2D, robot: Robot, vp: Viewport, selectedId: Option[String]): Unit =
    robot.shape match
      case ShapeType.Circle(radius) =>
        val isSelected = selectedId.contains(robot.id.toString)
        drawRobotBody(g2, robot, radius, vp, isSelected)
        drawRobotDirection(g2, robot, radius, vp)

  private def drawRobotBody(
      g2: Graphics2D,
      robot: Robot,
      radius: Double,
      vp: Viewport,
      isSelected: Boolean,
  ): Unit =
    import io.github.srs.utils.SimulationDefaults.DynamicEntity.Robot.{ normalStroke, selectionStroke }
    import io.github.srs.utils.SimulationDefaults.UI.{ Colors, Strokes }

    val x = vp.ox + (robot.position.x - radius) * vp.s
    val y = vp.oy + (robot.position.y - radius) * vp.s
    val d = 2 * radius * vp.s

    robotShape.setFrame(x, y, d, d)

    val colors =
      if isSelected then (Colors.robotSelected, Colors.robotSelectedDark, Colors.robotSelectedBorder)
      else (Colors.robotDefault, Colors.robotDefaultDark, Colors.robotDefaultBorder)

    val gradient = new RadialGradientPaint(
      (vp.ox + robot.position.x * vp.s).toFloat,
      (vp.oy + robot.position.y * vp.s).toFloat,
      (radius * vp.s).toFloat,
      Array(0f, 1f),
      Array(colors._1, colors._2),
    )

    g2.setPaint(gradient)
    g2.fill(robotShape)

    g2.setColor(Colors.robotShadow)
    g2.setStroke(new BasicStroke(Strokes.robotShadowStroke))
    g2.draw(robotShape)

    g2.setColor(colors._3)
    val strokeWidth = if isSelected then selectionStroke else normalStroke
    g2.setStroke(new BasicStroke(strokeWidth))
    g2.draw(robotShape)

  end drawRobotBody

  private def drawRobotDirection(g2: Graphics2D, robot: Robot, radius: Double, vp: Viewport): Unit =
    import SimulationDefaults.DynamicEntity.Robot.*

    val cx = (vp.ox + robot.position.x * vp.s).toInt
    val cy = (vp.oy + robot.position.y * vp.s).toInt
    val angle = robot.orientation.toRadians
    val length = radius * arrowLengthFactor * vp.s
    val width = math.max(minArrowWidth.toDouble, radius * vp.s * arrowWidthFactor)

    val arrow = createArrowPolygon(cx, cy, angle, length, width)
    g2.setColor(java.awt.Color.BLACK)
    g2.fillPolygon(arrow)

  private def createArrowPolygon(cx: Int, cy: Int, angle: Double, length: Double, width: Double): java.awt.Polygon =
    val (cosA, sinA) = (math.cos(angle), math.sin(angle))
    val halfWidth = width / 2

    new java.awt.Polygon(
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
